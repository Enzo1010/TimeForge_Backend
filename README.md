# TimeForge API

Backend para geração automática de grades horárias acadêmicas.

## Visão Geral

O TimeForge resolve o problema de montagem de horários escolares usando:

- **CSP** (Constraint Satisfaction Problem) com Backtracking
- **Heurística MRV** (Minimum Remaining Values) para ordenação de variáveis
- **First Fit Decreasing** para alocação de salas
- **Limites de segurança**: 200.000 iterações e timeout de 10 segundos para evitar travamentos em inputs grandes

O sistema gera grades sem conflitos de professor, turma e sala, respeitando disponibilidade e regras de capacidade/tipo de sala.

## Stack

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA + PostgreSQL
- Spring Security + JWT (JJWT 0.12.5)
- Spring Validation
- Bucket4j 8.10.1 (rate limiting)
- springdoc-openapi 2.8.5 (Swagger UI)
- Lombok
- Maven

## Autenticação e Autorização

A API utiliza JWT (JSON Web Token) para autenticação stateless.

### Roles

| Role | Permissões |
|------|------------|
| `ADMIN` | Leitura + escrita (GET, POST, PUT, DELETE) |
| `VIEWER` | Somente leitura (GET) |

### Endpoints públicos (sem token)

- `POST /auth/register` - Registro de novo usuário (role VIEWER)
- `POST /auth/login` - Login e obtenção do token JWT
- `/swagger-ui/**` - Documentação interativa
- `/v3/api-docs/**` - Especificação OpenAPI

### Como autenticar

1. Faça login em `POST /auth/login` com email e senha.
2. A resposta retorna o token JWT.
3. Inclua o header em todas as requisições autenticadas:
   ```
   Authorization: Bearer <token>
   ```

### Rate Limiting

| Endpoint | Limite | Janela |
|----------|--------|--------|
| `POST /auth/login` | 5 requisições | 1 minuto |
| `POST /auth/register` | 3 requisições | 10 minutos |

Ao exceder o limite, a API retorna `429 Too Many Requests`.

### Seed de usuários

O `DataSeeder` cria automaticamente um usuário admin na primeira execução:

- **Email**: `admin@timeforge.local`
- **Senha**: `admin123`
- **Role**: `ADMIN`

As credenciais são configuráveis em `application.yml`.

## Domínios Principais

- `Professor` - Docentes
- `Turma` - Turmas com capacidade
- `Disciplina` - Disciplinas com flag `requerLaboratorio`
- `Sala` - Salas com `TipoSala` (`COMUM` ou `LABORATORIO`)
- `SlotHorario` - Faixas de horário por dia da semana
- `DisponibilidadeProfessor` - Relação professor x slot
- `TurmaDisciplina` - Vínculo turma + disciplina + professor + carga horária
- `Aula` - Resultado da geração (turma + disciplina + professor + sala + slot)
- `Usuario` - Usuários do sistema com role (ADMIN/VIEWER)

## Regras de Negócio

- Não pode haver duas aulas no mesmo `slot` para o mesmo professor.
- Não pode haver duas aulas no mesmo `slot` para a mesma turma.
- Não pode haver duas aulas no mesmo `slot` para a mesma sala.
- Professor só pode ser alocado em slot com disponibilidade cadastrada.
- Sala precisa comportar a capacidade da turma.
- Disciplina que requer laboratório só pode usar sala `LABORATORIO`.

## Restrições no Banco (`aula`)

- `uk_sala_slot`: unicidade (`sala_id`, `slot_horario_id`)
- `uk_turma_slot`: unicidade (`turma_id`, `slot_horario_id`)
- `uk_prof_slot`: unicidade (`professor_id`, `slot_horario_id`)

## Fluxo de Geração

1. Carrega a turma e suas ofertas em `TurmaDisciplina`.
2. Expande cada oferta em aulas individuais pela `cargaHorariaSemanal`.
3. Monta domínios de `SlotHorario` e `Sala` compatíveis.
4. Aplica heurística MRV para ordenar variáveis.
5. Executa Backtracking com limite de 200k iterações / 10s.
6. Se sucesso, persiste a grade em `aula` substituindo a grade anterior da turma.
7. Se falha ou timeout, retorna a melhor solução parcial com diagnóstico.

Observação: o algoritmo monta a solução em memória e o service persiste em seguida, na mesma operação de negócio.

## Validações Implementadas

### API

- IDs de path e `turmaId` com `@Positive`.
- Campos obrigatórios com `@NotBlank`, `@NotNull`.
- Email com `@Email`, senha com `@Size(min = 6)`.
- `SlotHorarioRequestDTO` exige `horaInicio < horaFim`.
- Código de disciplina com unicidade verificada no service.

### Domínio

- `Turma.capacidade > 0`
- `Sala.capacidade > 0`
- `TurmaDisciplina.cargaHorariaSemanal > 0`
- `SlotHorario` com intervalo válido
- Validações defensivas no gerador e na persistência para referências obrigatórias.

## Padrão de Erros

As respostas de erro seguem payload único via `@RestControllerAdvice`:

- `timestamp`
- `status`
- `erro`
- `mensagem`
- `path`
- `errosValidacao` (lista opcional por campo)

Exemplo:

```json
{
  "timestamp": "2026-03-09T13:30:00Z",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Falha de validação nos campos da requisição.",
  "path": "/turmas",
  "errosValidacao": [
    {
      "campo": "capacidade",
      "mensagem": "Capacidade da turma deve ser maior que zero"
    }
  ]
}
```

Tipos de erro tratados: `MethodArgumentNotValidException`, `ConstraintViolationException`, `MethodArgumentTypeMismatchException`, `HttpMessageNotReadableException`, `DataIntegrityViolationException` e `ResponseStatusException`.

## Endpoints

### Autenticação

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `POST` | `/auth/register` | Registrar usuário | Não |
| `POST` | `/auth/login` | Login | Não |

### Professores

| Método | Rota | Auth |
|--------|------|------|
| `GET` | `/professores` | VIEWER / ADMIN |
| `GET` | `/professores/{id}` | VIEWER / ADMIN |
| `POST` | `/professores` | ADMIN |
| `PUT` | `/professores/{id}` | ADMIN |
| `DELETE` | `/professores/{id}` | ADMIN |

### Turmas

| Método | Rota | Auth |
|--------|------|------|
| `GET` | `/turmas` | VIEWER / ADMIN |
| `GET` | `/turmas/{id}` | VIEWER / ADMIN |
| `POST` | `/turmas` | ADMIN |
| `PUT` | `/turmas/{id}` | ADMIN |
| `DELETE` | `/turmas/{id}` | ADMIN |

### Disciplinas

| Método | Rota | Auth |
|--------|------|------|
| `GET` | `/disciplinas` | VIEWER / ADMIN |
| `GET` | `/disciplinas/{id}` | VIEWER / ADMIN |
| `POST` | `/disciplinas` | ADMIN |
| `PUT` | `/disciplinas/{id}` | ADMIN |
| `DELETE` | `/disciplinas/{id}` | ADMIN |

### Salas

| Método | Rota | Auth |
|--------|------|------|
| `GET` | `/salas` | VIEWER / ADMIN |
| `GET` | `/salas/{id}` | VIEWER / ADMIN |
| `POST` | `/salas` | ADMIN |
| `PUT` | `/salas/{id}` | ADMIN |
| `DELETE` | `/salas/{id}` | ADMIN |

### Slots de Horário

| Método | Rota | Auth |
|--------|------|------|
| `GET` | `/slothorarios` | VIEWER / ADMIN |
| `GET` | `/slothorarios/{id}` | VIEWER / ADMIN |
| `POST` | `/slothorarios` | ADMIN |
| `PUT` | `/slothorarios/{id}` | ADMIN |
| `DELETE` | `/slothorarios/{id}` | ADMIN |

### Disponibilidade de Professor

| Método | Rota | Auth |
|--------|------|------|
| `GET` | `/disponibilidades-professor` | VIEWER / ADMIN |
| `GET` | `/disponibilidades-professor/{id}` | VIEWER / ADMIN |
| `POST` | `/disponibilidades-professor` | ADMIN |
| `PUT` | `/disponibilidades-professor/{id}` | ADMIN |
| `DELETE` | `/disponibilidades-professor/{id}` | ADMIN |

### TurmaDisciplina

| Método | Rota | Auth |
|--------|------|------|
| `GET` | `/turmas-disciplinas` | VIEWER / ADMIN |
| `GET` | `/turmas-disciplinas/{id}` | VIEWER / ADMIN |
| `POST` | `/turmas-disciplinas` | ADMIN |
| `PUT` | `/turmas-disciplinas/{id}` | ADMIN |
| `DELETE` | `/turmas-disciplinas/{id}` | ADMIN |

### Schedule

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `POST` | `/schedule/generate` | Gerar grade (body) | ADMIN |
| `POST` | `/schedule/generate/{turmaId}` | Gerar grade (path) | ADMIN |
| `GET` | `/schedule/turma/{turmaId}` | Consultar grade | VIEWER / ADMIN |

## Contrato da API (v1)

- Contrato congelado em `09/03/2026`.
- Endpoints de atualização seguem padrão REST com `PUT` (não usar `PATCH`).
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Coleção Postman: `postman/TimeForge API - Contract v1.postman_collection.json`
- Regra de mudança: qualquer alteração de rota/verbo/estrutura de payload deve gerar nova versão do contrato.

## Exemplos

### Login

`POST /auth/login`

```json
{
  "email": "admin@timeforge.local",
  "senha": "admin123"
}
```

Resposta:

```json
{
  "token": "eyJhbGciOi...",
  "tipo": "Bearer",
  "usuarioId": 1,
  "nome": "Administrador",
  "email": "admin@timeforge.local",
  "role": "ADMIN"
}
```

### Gerar Grade por Body

`POST /schedule/generate`

```json
{
  "turmaId": 2
}
```

### Gerar Grade por Path Param

`POST /schedule/generate/2`

### Consultar Grade Persistida

`GET /schedule/turma/2`

### Criar Slot de Horário

`POST /slothorarios`

```json
{
  "diaSemana": "MONDAY",
  "horaInicio": "08:00:00",
  "horaFim": "09:00:00"
}
```

## Seed de Dados

`DataSeeder` popula dados mínimos quando o banco está vazio (quando `turma.count == 0`):

- Usuário admin (sempre, se não existir)
- Turmas
- Salas (com `tipoSala`)
- Slots semanais
- Professores
- Disponibilidades
- Disciplinas
- Vínculos `TurmaDisciplina`

Inclui cenário de bottleneck para testar soluções parciais do gerador.

## Como Rodar

### Pré-requisitos

- Java 21
- PostgreSQL com banco `timeforge_db` criado

### Configuração

Ajustar credenciais em `timeforge-api/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/timeforge_db
    username: postgres
    password: sua_senha
```

### Execução

Windows:

```bash
cd timeforge-api
./mvnw.cmd spring-boot:run
```

Linux/Mac:

```bash
cd timeforge-api
./mvnw spring-boot:run
```

Base URL padrão: `http://localhost:8080`

## Testes

### Executar

Windows:

```bash
cd timeforge-api
./mvnw.cmd test
```

Linux/Mac:

```bash
cd timeforge-api
./mvnw test
```

### Cobertura

O projeto possui **79 testes unitários** cobrindo:

| Camada | Classe | Testes |
|--------|--------|--------|
| Engine | ScheduleGenerator | 6 |
| Security | JwtService | 5 |
| Service | AuthService | 5 |
| Service | ProfessorService | 9 |
| Service | DisciplinaService | 11 |
| Service | SalaService | 7 |
| Service | TurmaService | 8 |
| Service | TurmaDisciplinaService | 10 |
| Service | DisponibilidadeProfessorService | 9 |
| Service | SlotHorarioService | 3 |
| Service | ScheduleService | 3 |
| Service | SchedulePersistenceService | 3 |

Cenários cobertos:

- Geração completa e parcial de grades (CSP + Backtracking)
- Indisponibilidade de professor e ausência de sala compatível
- Validações de capacidade, carga horária e intervalo de slot
- CRUD completo de todas as entidades
- Proteção de exclusão com vínculos (409 Conflict)
- Duplicidade de registros (código de disciplina, turma-disciplina, disponibilidade)
- Autenticação: login, registro, email duplicado, credenciais inválidas
- JWT: geração, validação, extração de claims, token expirado

## Estrutura

```text
timeforge-api/src/main/java/br/com/timeforge/timeforge_api
  |- config/          # OpenApiConfig, DataSeeder
  |- controller/      # REST controllers
  |- dto/
  |   |- request/     # DTOs de entrada com validação
  |   |- response/    # DTOs de saída
  |- engine/          # ScheduleGenerator (CSP + Backtracking)
  |- entity/          # Entidades JPA
  |- exception/       # ApiExceptionHandler global
  |- repository/      # Spring Data JPA repositories
  |- security/        # JWT, filters, SecurityConfig
  |- service/         # Lógica de negócio
```

## Licença

Uso acadêmico.
