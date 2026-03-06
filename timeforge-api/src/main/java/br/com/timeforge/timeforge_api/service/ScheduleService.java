package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.engine.ScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleGenerator scheduleGenerator;

    /**
     * Camada de aplicacao que delega a geracao para o engine.
     * Nesta fase, o resultado representa a grade em memoria.
     */
    public ScheduleGenerationResponseDTO gerarHorario(Long turmaId) {
        return scheduleGenerator.gerarHorario(turmaId);
    }
}
