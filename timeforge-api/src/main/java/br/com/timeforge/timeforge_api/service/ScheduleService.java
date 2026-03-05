package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.engine.ScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    /**
     * ScheduleService
     * - Camada de aplicação que orquestra a geração de horários.
     * - Deve buscar os dados do banco, chamar o engine e persistir o resultado.
     * Falta:
     * - Buscar dados (turma_disciplina, slots, salas, disponibilidades).
     * - Persistir as aulas geradas na tabela 'aula'.
     */


    private final ScheduleGenerator scheduleGenerator;

    public void gerarHorario(Long turmaId) {
        scheduleGenerator.gerarHorario(turmaId);
    }
}