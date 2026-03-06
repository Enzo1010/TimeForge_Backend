# TimeForge API

Backend para geracao automatica de grades horarias academicas.

## Visao Geral
O TimeForge resolve o problema de montagem de horarios escolares usando:
- CSP (Constraint Satisfaction Problem)
- Busca por Backtracking
- Heuristicas de ordenacao (MRV simplificado e First Fit Decreasing)

Com isso, o sistema gera grade sem conflitos de professor, turma e sala, respeitando disponibilidade e regras de capacidade/tipo de sala.

## Stack
- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Lombok

## Dominios Principais
- `Professor`
- `Turma`
- `Disciplina`
- `Sala` (`TipoSala`: `COMUM` ou `LABORATORIO`)
- `SlotHorario`
- `DisponibilidadeProfessor`
- `TurmaDisciplina`
- `Aula`

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
4. Aplica restricoes e executa Backtracking.
5. Se sucesso, persiste a grade em `aula` substituindo a grade anterior da turma.
6. Se falha de alocacao, retorna diagnostico com observacoes.

Observacao: o algoritmo monta a solucao em memoria e o service persiste em seguida, na mesma operacao de negocio.

## Validacoes Implementadas

### API
- IDs de path e `turmaId` com `@Positive`.
- `SlotHorarioRequestDTO` exige `horaInicio < horaFim`.

### Dominio
- `Turma.capacidade > 0`
- `Sala.capacidade > 0`
- `TurmaDisciplina.cargaHorariaSemanal > 0`
- `SlotHorario` com intervalo valido
- Validacoes defensivas no gerador e na persistencia para referencias obrigatorias.

## Endpoints

### Professores
- `GET /professores`
- `GET /professores/{id}`
- `POST /professores`
- `PATCH /professores/{id}`
- `DELETE /professores/{id}`

### Turmas
- `GET /turmas`
- `GET /turmas/{id}`
- `POST /turmas`
- `PATCH /turmas/{id}`
- `DELETE /turmas/{id}`

### Disciplinas
- `GET /disciplinas`
- `GET /disciplinas/{id}`
- `POST /disciplinas`
- `PATCH /disciplinas/{id}`
- `DELETE /disciplinas/{id}`

### Salas
- `GET /salas`
- `GET /salas/{id}`
- `POST /salas`
- `PATCH /salas/{id}`
- `DELETE /salas/{id}`

### Slots de Horario
- `GET /slothorarios`
- `GET /slothorarios/{id}`
- `POST /slothorarios`
- `PATCH /slothorarios/{id}`
- `DELETE /slothorarios/{id}`

### Schedule
- `POST /schedule/generate/{turmaId}`
- `POST /schedule/generate`
- `GET /schedule/turma/{turmaId}`

## Exemplos (Postman)

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
- turma
- salas (com `tipoSala`)
- slots semanais
- professores
- disponibilidades
- disciplinas
- vinculos `TurmaDisciplina`

## Como Rodar
1. Criar o banco `timeforge_db`.
2. Ajustar credenciais em `timeforge-api/src/main/resources/application.yml`.
3. Subir a API:

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
No diretorio `timeforge-api`:

Windows:

```bash
./mvnw.cmd test
```

Linux/Mac:

```bash
./mvnw test
```

Cobertura atual inclui cenarios de:
- geracao completa
- indisponibilidade de professor
- ausencia de sala compativel
- validacoes de capacidade/carga horaria
- validacao de intervalo de slot
- validacao de integridade na persistencia da grade

## Estrutura

```text
timeforge-api/src/main/java/br/com/timeforge/timeforge_api
  |- config
  |- controller
  |- dto
  |- engine
  |- entity
  |- repository
  |- service
```

## Proximos Passos Sugeridos
1. Criar endpoints CRUD para `DisponibilidadeProfessor` e `TurmaDisciplina`.
2. Adicionar `@ControllerAdvice` para padronizar erros de validacao.
3. Implementar traducoes de `DayOfWeek` na resposta (pt-BR), se for requisito de frontend.
4. Adicionar testes de integracao com Testcontainers para fluxo completo.

## Licenca
Uso academico.
