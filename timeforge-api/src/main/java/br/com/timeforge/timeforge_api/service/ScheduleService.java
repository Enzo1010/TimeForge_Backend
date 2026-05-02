package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ScheduleGenerationRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleTurmaResponseDTO;
import br.com.timeforge.timeforge_api.engine.ScheduleGenerator;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleGenerator scheduleGenerator;
    private final SchedulePersistenceService schedulePersistenceService;

    /**
     * Gera a grade, persiste em aula e retorna o resultado consolidado.
     */
    public ScheduleGenerationResponseDTO gerarHorario(ScheduleGenerationRequestDTO payload) {
        log.info("Iniciando geracao de horario para turmaId={}", payload.getTurmaId());
        ScheduleGenerationResponseDTO resultadoGeracao = scheduleGenerator.gerarHorario(payload);
        if (!Boolean.TRUE.equals(resultadoGeracao.getSucesso())) {
            log.warn(
                    "Geracao de horario finalizada sem sucesso para turmaId={}, mensagem={}",
                    payload.getTurmaId(),
                    resultadoGeracao.getMensagem()
            );
            return resultadoGeracao;
        }

        schedulePersistenceService.substituirGradeDaTurma(resultadoGeracao);
        log.info(
                "Horario gerado e persistido com sucesso para turmaId={}, totalAulas={}",
                resultadoGeracao.getTurmaId(),
                resultadoGeracao.getTotalAulasAlocadas()
        );

        List<String> observacoes = new ArrayList<>(Optional.ofNullable(resultadoGeracao.getObservacoes()).orElse(List.of()));
        observacoes.add("Grade persistida na tabela 'aula' com sucesso.");

        return ScheduleGenerationResponseDTO.builder()
                .sucesso(true)
                .mensagem("Grade gerada e persistida com sucesso.")
                .turmaId(resultadoGeracao.getTurmaId())
                .turmaNome(resultadoGeracao.getTurmaNome())
                .totalAulasNecessarias(resultadoGeracao.getTotalAulasNecessarias())
                .totalAulasAlocadas(resultadoGeracao.getTotalAulasAlocadas())
                .tempoBacktrackingMs(resultadoGeracao.getTempoBacktrackingMs())
                .aulas(resultadoGeracao.getAulas())
                .observacoes(observacoes)
                .build();
    }

    public ScheduleTurmaResponseDTO consultarGradeTurma(Long turmaId) {
        log.debug("Consultando grade persistida para turmaId={}", turmaId);
        return schedulePersistenceService.consultarGradeTurma(turmaId);
    }
}
