package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ScheduleGenerationRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleTurmaResponseDTO;
import br.com.timeforge.timeforge_api.engine.ScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleGenerator scheduleGenerator;
    private final SchedulePersistenceService schedulePersistenceService;

    /**
     * Gera a grade, persiste em aula e retorna o resultado consolidado.
     */
    public ScheduleGenerationResponseDTO gerarHorario(ScheduleGenerationRequestDTO payload) {
        ScheduleGenerationResponseDTO resultadoGeracao = scheduleGenerator.gerarHorario(payload);
        if (!Boolean.TRUE.equals(resultadoGeracao.getSucesso())) {
            return resultadoGeracao;
        }

        schedulePersistenceService.substituirGradeDaTurma(resultadoGeracao);

        List<String> observacoes = new ArrayList<>(Optional.ofNullable(resultadoGeracao.getObservacoes()).orElse(List.of()));
        observacoes.add("Grade persistida na tabela 'aula' com sucesso.");

        return ScheduleGenerationResponseDTO.builder()
                .sucesso(true)
                .mensagem("Grade gerada e persistida com sucesso.")
                .turmaId(resultadoGeracao.getTurmaId())
                .turmaNome(resultadoGeracao.getTurmaNome())
                .totalAulasNecessarias(resultadoGeracao.getTotalAulasNecessarias())
                .totalAulasAlocadas(resultadoGeracao.getTotalAulasAlocadas())
                .aulas(resultadoGeracao.getAulas())
                .observacoes(observacoes)
                .build();
    }

    public ScheduleTurmaResponseDTO consultarGradeTurma(Long turmaId) {
        return schedulePersistenceService.consultarGradeTurma(turmaId);
    }
}
