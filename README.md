# TimeForge API

Backend para geracao automatica de grades horarias academicas.
Projeto desenvolvido no contexto de Projeto Integrador (Ciencia da Computacao).

## Problema
Montar grade manualmente gera conflito, retrabalho e baixa previsibilidade.
O TimeForge recebe dados estruturados (turma, professor, disciplina, sala e horarios)
e calcula uma grade valida automaticamente.

## Objetivo
Gerar horarios sem conflito de:
- professor no mesmo slot
- turma no mesmo slot
- sala no mesmo slot

Tambem respeitando:
- disponibilidade de professor
- capacidade minima da sala para a turma
- requisito de laboratorio da disciplina (quando aplicavel)

## Stack
- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Lombok

## Arquitetura
- `controller`: endpoints HTTP
- `service`: orquestracao da aplicacao
- `engine`: algoritmo de geracao (CSP + Backtracking)
- `repository`: acesso a dados
- `entity`: modelo relacional JPA
- `dto`: contrato de entrada/saida da API

## Modelo de dominio
- `Professor`
- `Turma`
- `Disciplina`
- `Sala`
- `SlotHorario`
- `DisponibilidadeProfessor`
- `TurmaDisciplina`
- `Aula`

### Restricoes de unicidade na entidade `Aula`
- `uk_sala_slot`: impede duas aulas na mesma sala e slot
- `uk_turma_slot`: impede duas aulas da mesma turma no mesmo slot
- `uk_prof_slot`: impede duas aulas do mesmo professor no mesmo slot

## Algoritmo de geracao (estado atual)
O gerador foi implementado em memoria com abordagem de CSP.

1. Variaveis
- Cada aula individual derivada de `TurmaDisciplina` pela `cargaHorariaSemanal`.

2. Dominios
- Combinacoes de `SlotHorario` e `Sala` viaveis para cada aula.

3. Restricoes
- professor/slot unico
- turma/slot unico
- sala/slot unico
- professor precisa estar disponivel no slot
- sala precisa comportar a turma
- disciplina com laboratorio precisa de sala compativel

4. Busca
- Backtracking incremental com rollback.

5. Heuristicas
- Ordenacao de variaveis por menor disponibilidade de professor (MRV simplificado)
- First Fit Decreasing (FFD) para salas (maior capacidade primeiro)

6. Resultado atual
- Retorna grade completa quando existe solucao.
- Quando nao existe solucao completa, retorna melhor parcial com observacoes.
- Nesta etapa ainda nao persiste na tabela `aula`.

## Endpoints

### Professores
- `GET /professores`
- `GET /professores/{id}`
- `POST /professores`
- `PATCH /professores/{id}`
- `DELETE /professores/{id}`

### Slots de horario
- `GET /slothorarios`
- `GET /slothorarios/{id}`
- `POST /slothorarios`
- `PATCH /slothorarios/{id}`
- `DELETE /slothorarios/{id}`

### Geracao de grade
- `POST /schedule/generate/{turmaId}`

Retorno: `ScheduleGenerationResponseDTO`, com:
- status (`sucesso`, `mensagem`)
- metadados da turma
- total necessario x total alocado
- lista de aulas alocadas (`ScheduleAulaResponseDTO`)
- observacoes de diagnostico

## Execucao local
1. Criar banco PostgreSQL `timeforge_db`.
2. Ajustar credenciais em `timeforge-api/src/main/resources/application.yml`.
3. Subir API:

```bash
cd timeforge-api
./mvnw.cmd spring-boot:run
```

4. Base local: `http://localhost:8080`

## Testes
Na pasta `timeforge-api`:

```bash
./mvnw.cmd test
```

Inclui testes unitarios do gerador para:
- cenario com solucao completa
- cenario sem disponibilidade
- cenario sem sala compativel

## Seed de dados
`DataSeeder` popula o banco (quando vazio) com dados minimos:
- 1 turma
- salas
- slots de segunda a sexta
- professores e disponibilidades
- disciplinas
- ofertas `TurmaDisciplina`

## Estrutura do projeto

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

## Proximos passos
1. Persistir grade final na tabela `aula` em transacao.
2. Expor endpoint para consultar grade persistida por turma.
3. Adicionar testes de integracao com banco isolado para pipeline CI.
4. Evoluir modelo de sala para flag explicita de laboratorio (em vez de convencao por nome).

## Licenca
Uso academico.
