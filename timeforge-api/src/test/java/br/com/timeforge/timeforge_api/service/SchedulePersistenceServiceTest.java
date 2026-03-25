package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.ScheduleAulaResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleTurmaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Aula;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulePersistenceServiceTest {

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private SlotHorarioRepository slotHorarioRepository;

    @InjectMocks
    private SchedulePersistenceService schedulePersistenceService;

    @Test
    void deveSubstituirGradeDaTurmaEmTransacao() {
        Turma turma = Turma.builder().id(2L).nome("9A").capacidade(30).build();
        Disciplina disciplina = Disciplina.builder().id(1L).nome("Matematica").build();
        Professor professor = Professor.builder().id(10L).nome("Joao").build();
        Sala sala = Sala.builder().id(20L).nome("Sala 101").capacidade(35).build();
        SlotHorario slot = SlotHorario.builder()
                .id(30L)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.parse("08:00"))
                .horaFim(LocalTime.parse("09:00"))
                .build();

        ScheduleAulaResponseDTO aulaResponse = ScheduleAulaResponseDTO.builder()
                .indiceAula(1)
                .turmaDisciplinaId(100L)
                .disciplinaId(1L)
                .professorId(10L)
                .turmaId(2L)
                .salaId(20L)
                .slotHorarioId(30L)
                .build();

        ScheduleGenerationResponseDTO geracao = ScheduleGenerationResponseDTO.builder()
                .sucesso(true)
                .turmaId(2L)
                .turmaNome("9A")
                .totalAulasNecessarias(1)
                .totalAulasAlocadas(1)
                .aulas(List.of(aulaResponse))
                .build();

        when(turmaRepository.findById(2L)).thenReturn(Optional.of(turma));
        when(disciplinaRepository.findAllById(eq(java.util.Set.of(1L)))).thenReturn(List.of(disciplina));
        when(professorRepository.findAllById(eq(java.util.Set.of(10L)))).thenReturn(List.of(professor));
        when(salaRepository.findAllById(eq(java.util.Set.of(20L)))).thenReturn(List.of(sala));
        when(slotHorarioRepository.findAllById(eq(java.util.Set.of(30L)))).thenReturn(List.of(slot));

        schedulePersistenceService.substituirGradeDaTurma(geracao);

        verify(aulaRepository, times(1)).deleteAllByTurmaId(2L);
        verify(aulaRepository, times(1)).flush();
        verify(aulaRepository, times(1)).saveAllAndFlush(anyList());

        ArgumentCaptor<List<Aula>> captor = ArgumentCaptor.forClass(List.class);
        verify(aulaRepository).saveAllAndFlush(captor.capture());
        List<Aula> aulasPersistidas = captor.getValue();

        assertEquals(1, aulasPersistidas.size());
        Aula aula = aulasPersistidas.get(0);
        assertEquals(2L, aula.getTurma().getId());
        assertEquals(1L, aula.getDisciplina().getId());
        assertEquals(10L, aula.getProfessor().getId());
        assertEquals(20L, aula.getSala().getId());
        assertEquals(30L, aula.getSlotHorario().getId());
    }

    @Test
    void deveConsultarGradePersistidaPorTurma() {
        Turma turma = Turma.builder().id(2L).nome("9A").capacidade(30).build();
        Disciplina disciplina = Disciplina.builder().id(1L).nome("Matematica").build();
        Professor professor = Professor.builder().id(10L).nome("Joao").build();
        Sala sala = Sala.builder().id(20L).nome("Sala 101").capacidade(35).build();
        SlotHorario slot = SlotHorario.builder()
                .id(30L)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.parse("08:00"))
                .horaFim(LocalTime.parse("09:00"))
                .build();

        Aula aula = Aula.builder()
                .id(1000L)
                .turma(turma)
                .disciplina(disciplina)
                .professor(professor)
                .sala(sala)
                .slotHorario(slot)
                .build();

        when(turmaRepository.findById(2L)).thenReturn(Optional.of(turma));
        when(aulaRepository.findByTurmaIdOrderBySlotHorario_DiaSemanaAscSlotHorario_HoraInicioAscSlotHorario_HoraFimAsc(2L))
                .thenReturn(List.of(aula));

        ScheduleTurmaResponseDTO resposta = schedulePersistenceService.consultarGradeTurma(2L);

        assertNotNull(resposta);
        assertEquals(2L, resposta.getTurmaId());
        assertEquals("9A", resposta.getTurmaNome());
        assertEquals(1, resposta.getTotalAulas());
        assertEquals(1, resposta.getAulas().size());
        assertEquals("Matematica", resposta.getAulas().get(0).getDisciplinaNome());
    }

    @Test
    void deveLancarBadRequestQuandoAulaGeradaTemTurmaDiferenteDaRequisitada() {
        Turma turma = Turma.builder().id(2L).nome("9A").capacidade(30).build();
        when(turmaRepository.findById(2L)).thenReturn(Optional.of(turma));

        ScheduleAulaResponseDTO aulaInvalida = ScheduleAulaResponseDTO.builder()
                .indiceAula(1)
                .turmaDisciplinaId(100L)
                .disciplinaId(1L)
                .professorId(10L)
                .turmaId(99L)
                .salaId(20L)
                .slotHorarioId(30L)
                .build();

        ScheduleGenerationResponseDTO geracao = ScheduleGenerationResponseDTO.builder()
                .sucesso(true)
                .turmaId(2L)
                .aulas(List.of(aulaInvalida))
                .build();

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> schedulePersistenceService.substituirGradeDaTurma(geracao)
        );

        assertEquals(400, exception.getStatus().value());
        verify(aulaRepository, times(0)).deleteAllByTurmaId(2L);
    }
}
