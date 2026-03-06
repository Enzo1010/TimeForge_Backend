# TimeForge API

Sistema para geração automática de grades horárias acadêmicas, desenvolvido no Projeto Integrador do 3º semestre de Ciência da Computação.

## Problema
Montar horário escolar manualmente gera conflitos e toma tempo. O TimeForge resolve esse problema gerando a grade automaticamente com base em dados estruturados de:

- turmas
- professores
- disciplinas
- salas
- slots de horário
- disponibilidade dos professores

## Objetivo
Gerar horários de aula:

- sem conflitos de turma, professor e sala
- respeitando disponibilidades
- com consistência de dados no banco

## Contexto acadêmico

- Disciplina: Projeto Integrador (3º semestre)
- Equipe: 3 integrantes
- Papel deste repositório: backend da API de geração de horários

## Stack e arquitetura

- Java 21
- Spring Boot 4.0.3
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Lombok

Arquitetura em camadas:

- `controller`: endpoints HTTP
- `service`: orquestração de regras de aplicação
- `engine`: lógica de geração do horário
- `repository`: acesso a dados
- `entity`: entidades JPA
- `dto`: classes

## Modelo de domínio

- `Professor`: docentes
- `Turma`: grupos de alunos
- `Disciplina`: catálogo de matérias
- `Sala`: espaços físicos com capacidade
- `SlotHorario`: intervalos fixos de tempo por dia da semana
- `DisponibilidadeProfessor`: relação professor x slot permitido
- `TurmaDisciplina`: vínculo turma + disciplina + professor + carga horária semanal
- `Aula`: resultado final da geração (disciplina, professor, turma, sala, slot)

### Restrições de unicidade em `Aula`

- não permite 2 aulas na mesma sala no mesmo slot
- não permite 2 aulas da mesma turma no mesmo slot
- não permite 2 aulas do mesmo professor no mesmo slot

## Abordagem algorítmica

O problema é tratado como CSP (Constraint Satisfaction Problem) com busca por Backtracking.

1. Variáveis:
- cada aula individual a ser alocada (expandida a partir de `TurmaDisciplina` e carga horária semanal)

2. Domínios:
- combinações válidas de `SlotHorario` e `Sala`

3. Restrições:
- sem conflito de sala, turma e professor no mesmo slot
- professor precisa estar disponível no slot
- sala precisa suportar capacidade da turma

4. Busca:
- Backtracking incremental
- ao detectar conflito, desfaz a última decisão e tenta nova combinação

5. Heurística de sala:
- First Fit Decreasing (FFD) para alocar turmas em salas que comportem seu tamanho

## Estado atual do projeto

- modelagem relacional principal implementada
- endpoints básicos de `Professor` e `SlotHorario` implementados
- endpoint de geração criado: `POST /schedule/generate/{turmaId}`
- seeder automático para ambiente local (`DataSeeder`)
- engine de geração em fase inicial (expande variáveis de aula para o CSP)

Observação: a persistência completa da grade final em `Aula` ainda está em evolução.

## Endpoints disponíveis

### Professor

- `GET /professores`
- `GET /professores/{id}`
- `POST /professores`
- `PATCH /professores/{id}`
- `DELETE /professores/{id}`

### SlotHorario

- `GET /slothorarios`
- `GET /slothorarios/{id}`
- `POST /slothorarios`
- `PATCH /slothorarios/{id}`
- `DELETE /slothorarios/{id}`

### Geração de horário

- `POST /schedule/generate/{turmaId}`

## Como executar localmente

1. Criar banco PostgreSQL:
- banco: `timeforge_db`

2. Ajustar credenciais em `src/main/resources/application.yml`:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

3. Executar a aplicação:

```bash
./mvnw.cmd spring-boot:run
```

4. API disponível em:
- `http://localhost:8080`

## Seed de dados

Ao subir a aplicação, o `DataSeeder` popula o banco (se ainda vazio) com:

- 1 turma
- salas
- slots de segunda a sexta
- professores e disponibilidades
- disciplinas
- vínculos `TurmaDisciplina`

## Estrutura do projeto

```text
src/main/java/br/com/timeforge/timeforge_api
  |- config
  |- controller
  |- domain
  |- engine
  |- repository
  |- service
```

## Roadmap até a entrega final

1. Implementar backtracking completo para alocação de aulas.
2. Integrar FFD no fluxo de escolha de sala.
3. Persistir grade final na tabela `aula`.
4. Tratar explicitamente cenários sem solução.
5. Criar testes de integração e cenários de conflito.
6. Refinar tratamento de erros e padrão de resposta da API.

## Licença

Uso acadêmico para a disciplina de Projeto Integrador.
