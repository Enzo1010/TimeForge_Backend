# TimeForge API

Backend para geracao automatica de grades horarias academicas.

## Visao Geral

O TimeForge resolve o problema de montagem de horarios escolares usando:

- **CSP** (Constraint Satisfaction Problem) com Backtracking
- **Heuristica MRV** (Minimum Remaining Values) para ordenacao de variaveis
- **First Fit Decreasing** para alocacao de salas
- **Limites de seguranca**: 200.000 iteracoes e timeout de 10 segundos para evitar travamentos em inputs grandes

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

## Autenticacao e Autorizacao

A API utiliza JWT (JSON Web Token) para autenticacao stateless.

### Roles

| Role | Permissoes |
|------|------------|
| `ADMIN` | Leitura + escrita (GET, POST, PUT, DELETE) |
| `VIEWER` | Somente leitura (GET) |

### Endpoints publicos (sem token)

- `POST /auth/register` - Registro de novo usuario (role VIEWER)
- `POST /auth/login` - Login e obtencao do token JWT
- `/swagger-ui/**` - Documentacao interativa
- `/v3/api-docs/**` - Especificacao OpenAPI

### Como autenticar

1. Faca login em `POST /auth/login` com email e senha.
2. A resposta retorna o token JWT.
3. Inclua o header em todas as requisicoes autenticadas:
   ```
   Authorization: Bearer <token>
   ```

### Rate Limiting

| Endpoint | Limite | Janela |
|----------|--------|--------|
| `POST /auth/login` | 5 requisicoes | 1 minuto |
| `POST /auth/register` | 3 requisicoes | 10 minutos |

Ao exceder o limite, a API retorna `429 Too Many Requests`.

### Seed de usuarios

O `DataSeeder` cria automaticamente um usuario admin na primeira execucao:

- **Email**: `admin@timeforge.local`
- **Senha**: `admin123`
- **Role**: `ADMIN`

As credenciais sao configuraveis em `application.yml`.

## Dominios Principais

- `Professor` - Docentes
- `Turma` - Turmas com capacidade
- `Disciplina` - Disciplinas com flag `requerLaboratorio`
- `Sala` - Salas com `TipoSala` (`COMUM` ou `LABORATORIO`)
- `SlotHorario` - Faixas de horario por dia da semana
- `DisponibilidadeProfessor` - Relacao professor x slot
- `TurmaDisciplina` - Vinculo turma + disciplina + professor + carga horaria
- `Aula` - Resultado da geracao (turma + disciplina + professor + sala + slot)
- `Usuario` - Usuarios do sistema com role (ADMIN/VIEWER)

## Regras de Negocio

- Nao pode haver duas aulas no mesmo `slot` para o mesmo professor.
- Nao pode haver duas aulas no mesmo `slot` para a mesma turma.
- Nao pode haver duas aulas no mesmo `slot` para a mesma sala.
- Professor so pode ser alocado em slot com disponibilidade cadastrada.
- Sala precisa comportar a capacidade da turma.
- Disciplina que requer laboratorio so pode usar sala `LABORATORIO`.

## Restricoes no Banco (`aula`)

- `uk_sala_slot`: unicidade (`sala_id`, `slot_horario_id`)
- `uk_turma_slot`: unicidade (`turma_id`, `slot_horario_id`)
- `uk_prof_slot`: unicidade (`professor_id`, `slot_horario_id`)

## Fluxo de Geracao

1. Carrega a turma e suas ofertas em `TurmaDisciplina`.
2. Expande cada oferta em aulas individuais pela `cargaHorariaSemanal`.
3. Monta dominios de `SlotHorario` e `Sala` compativeis.
4. Aplica heuristica MRV para ordenar variaveis.
5. Executa Backtracking com limite de 200k iteracoes / 10s.
6. Se sucesso, persiste a grade em `aula` substituindo a grade anterior da turma.
7. Se falha ou timeout, retorna a melhor solucao parcial com diagnostico.

Observacao: o algoritmo monta a solucao em memoria e o service persiste em seguida, na mesma operacao de negocio.

## Validacoes Implementadas

### API

- IDs de path e `turmaId` com `@Positive`.
- Campos obrigatorios com `@NotBlank`, `@NotNull`.
- Email com `@Email`, senha com `@Size(min = 6)`.
- `SlotHorarioRequestDTO` exige `horaInicio < horaFim`.
- Codigo de disciplina com unicidade verificada no service.

### Dominio

- `Turma.capacidade > 0`
- `Sala.capacidade > 0`
- `TurmaDisciplina.cargaHorariaSemanal > 0`
- `SlotHorario` com intervalo valido
- Validacoes defensivas no gerador e na persistencia para referencias obrigatorias.

## Padrao de Erros

As respostas de erro seguem payload unico via `@RestControllerAdvice`:

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
  "mensagem": "Falha de validacao nos campos da requisicao.",
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

### Autenticacao

| Metodo | Rota | Descricao | Auth |
|--------|------|-----------|------|
| `POST` | `/auth/register` | Registrar usuario | Nao |
| `POST` | `/auth/login` | Login | Nao |

### Professores

| Metodo | Rota | Auth |
|--------|------|------|
| `GET` | `/professores` | VIEWER / ADMIN |
| `GET` | `/professores/{id}` | VIEWER / ADMIN |
| `POST` | `/professores` | ADMIN |
| `PUT` | `/professores/{id}` | ADMIN |
| `DELETE` | `/professores/{id}` | ADMIN |

### Turmas

| Metodo | Rota | Auth |
|--------|------|------|
| `GET` | `/turmas` | VIEWER / ADMIN |
| `GET` | `/turmas/{id}` | VIEWER / ADMIN |
| `POST` | `/turmas` | ADMIN |
| `PUT` | `/turmas/{id}` | ADMIN |
| `DELETE` | `/turmas/{id}` | ADMIN |

### Disciplinas

| Metodo | Rota | Auth |
|--------|------|------|
| `GET` | `/disciplinas` | VIEWER / ADMIN |
| `GET` | `/disciplinas/{id}` | VIEWER / ADMIN |
| `POST` | `/disciplinas` | ADMIN |
| `PUT` | `/disciplinas/{id}` | ADMIN |
| `DELETE` | `/disciplinas/{id}` | ADMIN |

### Salas

| Metodo | Rota | Auth |
|--------|------|------|
| `GET` | `/salas` | VIEWER / ADMIN |
| `GET` | `/salas/{id}` | VIEWER / ADMIN |
| `POST` | `/salas` | ADMIN |
| `PUT` | `/salas/{id}` | ADMIN |
| `DELETE` | `/salas/{id}` | ADMIN |

### Slots de Horario

| Metodo | Rota | Auth |
|--------|------|------|
| `GET` | `/slothorarios` | VIEWER / ADMIN |
| `GET` | `/slothorarios/{id}` | VIEWER / ADMIN |
| `POST` | `/slothorarios` | ADMIN |
| `PUT` | `/slothorarios/{id}` | ADMIN |
| `DELETE` | `/slothorarios/{id}` | ADMIN |

### Disponibilidade de Professor

| Metodo | Rota | Auth |
|--------|------|------|
| `GET` | `/disponibilidades-professor` | VIEWER / ADMIN |
| `GET` | `/disponibilidades-professor/{id}` | VIEWER / ADMIN |
| `POST` | `/disponibilidades-professor` | ADMIN |
| `PUT` | `/disponibilidades-professor/{id}` | ADMIN |
| `DELETE` | `/disponibilidades-professor/{id}` | ADMIN |

### TurmaDisciplina

| Metodo | Rota | Auth |
|--------|------|------|
| `GET` | `/turmas-disciplinas` | VIEWER / ADMIN |
| `GET` | `/turmas-disciplinas/{id}` | VIEWER / ADMIN |
| `POST` | `/turmas-disciplinas` | ADMIN |
| `PUT` | `/turmas-disciplinas/{id}` | ADMIN |
| `DELETE` | `/turmas-disciplinas/{id}` | ADMIN |

### Schedule

| Metodo | Rota | Descricao | Auth |
|--------|------|-----------|------|
| `POST` | `/schedule/generate` | Gerar grade (body) | ADMIN |
| `POST` | `/schedule/generate/{turmaId}` | Gerar grade (path) | ADMIN |
| `GET` | `/schedule/turma/{turmaId}` | Consultar grade | VIEWER / ADMIN |

## Contrato da API (v1)

- Contrato congelado em `09/03/2026`.
- Endpoints de atualizacao seguem padrao REST com `PUT` (nao usar `PATCH`).
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Colecao Postman: `postman/TimeForge API - Contract v1.postman_collection.json`
- Regra de mudanca: qualquer alteracao de rota/verbo/estrutura de payload deve gerar nova versao do contrato.

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

### Criar Slot de Horario

`POST /slothorarios`

```json
{
  "diaSemana": "MONDAY",
  "horaInicio": "08:00:00",
  "horaFim": "09:00:00"
}
```

## Seed de Dados

`DataSeeder` popula dados minimos quando o banco esta vazio (quando `turma.count == 0`):

- Usuario admin (sempre, se nao existir)
- Turmas
- Salas (com `tipoSala`)
- Slots semanais
- Professores
- Disponibilidades
- Disciplinas
- Vinculos `TurmaDisciplina`

Inclui cenario de bottleneck para testar solucoes parciais do gerador.

## Como Rodar

### Pre-requisitos

- Java 21
- PostgreSQL com banco `timeforge_db` criado

### Configuracao

Ajustar credenciais em `timeforge-api/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/timeforge_db
    username: postgres
    password: sua_senha
```

### Execucao

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

Base URL padrao: `http://localhost:8080`

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

O projeto possui **79 testes unitarios** cobrindo:

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

Cenarios cobertos:

- Geracao completa e parcial de grades (CSP + Backtracking)
- Indisponibilidade de professor e ausencia de sala compativel
- Validacoes de capacidade, carga horaria e intervalo de slot
- CRUD completo de todas as entidades
- Protecao de exclusao com vinculos (409 Conflict)
- Duplicidade de registros (codigo de disciplina, turma-disciplina, disponibilidade)
- Autenticacao: login, registro, email duplicado, credenciais invalidas
- JWT: geracao, validacao, extracao de claims, token expirado

## Estrutura

```text
timeforge-api/src/main/java/br/com/timeforge/timeforge_api
  |- config/          # OpenApiConfig, DataSeeder
  |- controller/      # REST controllers
  |- dto/
  |   |- request/     # DTOs de entrada com validacao
  |   |- response/    # DTOs de saida
  |- engine/          # ScheduleGenerator (CSP + Backtracking)
  |- entity/          # Entidades JPA
  |- exception/       # ApiExceptionHandler global
  |- repository/      # Spring Data JPA repositories
  |- security/        # JWT, filters, SecurityConfig
  |- service/         # Logica de negocio
```

## Licenca

Uso academico.
