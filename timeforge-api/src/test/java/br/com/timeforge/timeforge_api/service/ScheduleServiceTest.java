package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ScheduleGenerationRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleAulaResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleTurmaResponseDTO;
import br.com.timeforge.timeforge_api.engine.ScheduleGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleGenerator scheduleGenerator;

    @Mock
    private SchedulePersistenceService schedulePersistenceService;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void deveGerarEPersistirQuandoSucesso() {
        ScheduleGenerationRequestDTO payload = new ScheduleGenerationRequestDTO(1L);

        ScheduleGenerationResponseDTO geracao = ScheduleGenerationResponseDTO.builder()
                .sucesso(true)
                .mensagem("Grade gerada com sucesso (em memoria).")
                .turmaId(1L)
                .turmaNome("Turma A")
                .totalAulasNecessarias(3)
                .totalAulasAlocadas(3)
                .tempoBacktrackingMs(42L)
                .aulas(List.of(ScheduleAulaResponseDTO.builder().indiceAula(1).build()))
                .observacoes(List.of("obs1"))
                .build();

        when(scheduleGenerator.gerarHorario(payload)).thenReturn(geracao);

        ScheduleGenerationResponseDTO result = scheduleService.gerarHorario(payload);

        assertTrue(result.getSucesso());
        assertEquals("Grade gerada e persistida com sucesso.", result.getMensagem());
        assertEquals(42L, result.getTempoBacktrackingMs());
        assertTrue(result.getObservacoes().contains("Grade persistida na tabela 'aula' com sucesso."));
        verify(schedulePersistenceService).substituirGradeDaTurma(geracao);
    }

    @Test
    void deveRetornarFalhaQuandoGeracaoFalha() {
        ScheduleGenerationRequestDTO payload = new ScheduleGenerationRequestDTO(1L);

        ScheduleGenerationResponseDTO geracao = ScheduleGenerationResponseDTO.builder()
                .sucesso(false)
                .mensagem("Nao foi possivel montar a grade completa sem conflitos.")
                .turmaId(1L)
                .turmaNome("Turma A")
                .totalAulasNecessarias(3)
                .totalAulasAlocadas(1)
                .aulas(List.of())
                .build();

        when(scheduleGenerator.gerarHorario(payload)).thenReturn(geracao);

        ScheduleGenerationResponseDTO result = scheduleService.gerarHorario(payload);

        assertFalse(result.getSucesso());
        verify(schedulePersistenceService, never()).substituirGradeDaTurma(any());
    }

    @Test
    void deveConsultarGradeTurma() {
        ScheduleTurmaResponseDTO response = ScheduleTurmaResponseDTO.builder()
                .turmaId(1L).turmaNome("Turma A").totalAulas(3).aulas(List.of()).build();

        when(schedulePersistenceService.consultarGradeTurma(1L)).thenReturn(response);

        ScheduleTurmaResponseDTO result = scheduleService.consultarGradeTurma(1L);

        assertEquals(1L, result.getTurmaId());
        assertEquals(3, result.getTotalAulas());
    }
}
