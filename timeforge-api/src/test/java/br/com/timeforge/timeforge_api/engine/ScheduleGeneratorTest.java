package br.com.timeforge.timeforge_api.engine;

import br.com.timeforge.timeforge_api.dto.request.ScheduleGenerationRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.entity.TipoSala;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleGeneratorTest {

        @Mock
        private TurmaRepository turmaRepository;

        @Mock
        private TurmaDisciplinaRepository turmaDisciplinaRepository;

        @Mock
        private SlotHorarioRepository slotHorarioRepository;

        @Mock
        private SalaRepository salaRepository;

        @Mock
        private DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;

        @Mock
        private AulaRepository aulaRepository;

        @InjectMocks
        private ScheduleGenerator scheduleGenerator;

        @Test
        void deveGerarGradeCompletaQuandoDadosSaoCompativeis() {
                Turma turma = Turma.builder().id(1L).nome("9A").capacidade(30).build();

                Professor profJoao = Professor.builder().id(10L).nome("Joao").build();
                Professor profMaria = Professor.builder().id(11L).nome("Maria").build();

                Disciplina matematica = Disciplina.builder().id(100L).nome("Matematica").requerLaboratorio(false)
                                .build();
                Disciplina portugues = Disciplina.builder().id(101L).nome("Portugues").requerLaboratorio(false).build();

                TurmaDisciplina tdMat = TurmaDisciplina.builder()
                                .id(1000L)
                                .turma(turma)
                                .professor(profJoao)
                                .disciplina(matematica)
                                .cargaHorariaSemanal(2)
                                .build();

                TurmaDisciplina tdPor = TurmaDisciplina.builder()
                                .id(1001L)
                                .turma(turma)
                                .professor(profMaria)
                                .disciplina(portugues)
                                .cargaHorariaSemanal(1)
                                .build();

                SlotHorario slot1 = slot(1L, DayOfWeek.MONDAY, "08:00", "09:00");
                SlotHorario slot2 = slot(2L, DayOfWeek.MONDAY, "09:00", "10:00");
                SlotHorario slot3 = slot(3L, DayOfWeek.TUESDAY, "08:00", "09:00");

                Sala salaGrande = Sala.builder().id(20L).nome("Sala 201").capacidade(40).tipoSala(TipoSala.COMUM)
                                .build();
                Sala salaPequena = Sala.builder().id(21L).nome("Sala 101").capacidade(30).tipoSala(TipoSala.COMUM)
                                .build();

                when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
                when(turmaDisciplinaRepository.findByTurmaId(1L)).thenReturn(List.of(tdMat, tdPor));
                when(slotHorarioRepository.findAll()).thenReturn(List.of(slot1, slot2, slot3));
                when(salaRepository.findAll()).thenReturn(List.of(salaPequena, salaGrande));
                when(aulaRepository.findByTurmaIdNot(1L)).thenReturn(List.of());
                when(disponibilidadeProfessorRepository.findByProfessorIdIn(anyCollection())).thenReturn(List.of(
                                disponibilidade(500L, profJoao, slot1),
                                disponibilidade(501L, profJoao, slot2),
                                disponibilidade(502L, profJoao, slot3),
                                disponibilidade(503L, profMaria, slot1),
                                disponibilidade(504L, profMaria, slot2),
                                disponibilidade(505L, profMaria, slot3)));

                ScheduleGenerationResponseDTO resultado = scheduleGenerator
                                .gerarHorario(new ScheduleGenerationRequestDTO(1L));

                assertTrue(resultado.getSucesso());
                assertEquals(3, resultado.getTotalAulasNecessarias());
                assertEquals(3, resultado.getTotalAulasAlocadas());
                assertEquals(3, resultado.getAulas().size());

                // Garante ausencia de conflito professor+slot no resultado.
                Set<String> professorSlot = resultado.getAulas().stream()
                                .map(aula -> aula.getProfessorId() + ":" + aula.getSlotHorarioId())
                                .collect(Collectors.toSet());
                assertEquals(resultado.getAulas().size(), professorSlot.size());

                // Garante ausencia de conflito turma+slot no resultado.
                Set<String> turmaSlot = resultado.getAulas().stream()
                                .map(aula -> aula.getTurmaId() + ":" + aula.getSlotHorarioId())
                                .collect(Collectors.toSet());
                assertEquals(resultado.getAulas().size(), turmaSlot.size());

                // Garante ausencia de conflito sala+slot no resultado.
                Set<String> salaSlot = resultado.getAulas().stream()
                                .map(aula -> aula.getSalaId() + ":" + aula.getSlotHorarioId())
                                .collect(Collectors.toSet());
                assertEquals(resultado.getAulas().size(), salaSlot.size());
        }

        @Test
        void deveFalharQuandoProfessorNaoTemDisponibilidade() {
                Turma turma = Turma.builder().id(1L).nome("9A").capacidade(30).build();
                Professor profJoao = Professor.builder().id(10L).nome("Joao").build();
                Disciplina matematica = Disciplina.builder().id(100L).nome("Matematica").requerLaboratorio(false)
                                .build();

                TurmaDisciplina tdMat = TurmaDisciplina.builder()
                                .id(1000L)
                                .turma(turma)
                                .professor(profJoao)
                                .disciplina(matematica)
                                .cargaHorariaSemanal(1)
                                .build();

                SlotHorario slot1 = slot(1L, DayOfWeek.MONDAY, "08:00", "09:00");
                Sala sala = Sala.builder().id(20L).nome("Sala 101").capacidade(35).tipoSala(TipoSala.COMUM).build();

                when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
                when(turmaDisciplinaRepository.findByTurmaId(1L)).thenReturn(List.of(tdMat));
                when(slotHorarioRepository.findAll()).thenReturn(List.of(slot1));
                when(salaRepository.findAll()).thenReturn(List.of(sala));
                when(aulaRepository.findByTurmaIdNot(1L)).thenReturn(List.of());
                when(disponibilidadeProfessorRepository.findByProfessorIdIn(eq(Set.of(10L)))).thenReturn(List.of());

                ScheduleGenerationResponseDTO resultado = scheduleGenerator
                                .gerarHorario(new ScheduleGenerationRequestDTO(1L));

                assertFalse(resultado.getSucesso());
                assertEquals(1, resultado.getTotalAulasNecessarias());
                assertEquals(0, resultado.getTotalAulasAlocadas());
                assertTrue(resultado.getAulas().isEmpty());
                assertNotNull(resultado.getObservacoes());
                assertTrue(resultado.getObservacoes().stream()
                                .anyMatch(obs -> obs.contains("nao possui disponibilidade")));
        }

        @Test
        void deveFalharQuandoDisciplinaExigeLaboratorioMasNaoExisteSalaCompativel() {
                Turma turma = Turma.builder().id(1L).nome("9A").capacidade(30).build();
                Professor profCarlos = Professor.builder().id(12L).nome("Carlos").build();
                Disciplina informatica = Disciplina.builder().id(200L).nome("Informatica").requerLaboratorio(true)
                                .build();

                TurmaDisciplina tdInf = TurmaDisciplina.builder()
                                .id(1002L)
                                .turma(turma)
                                .professor(profCarlos)
                                .disciplina(informatica)
                                .cargaHorariaSemanal(1)
                                .build();

                SlotHorario slot1 = slot(1L, DayOfWeek.MONDAY, "08:00", "09:00");
                Sala salaComum = Sala.builder().id(30L).nome("Sala 101").capacidade(40).tipoSala(TipoSala.COMUM)
                                .build();

                when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
                when(turmaDisciplinaRepository.findByTurmaId(1L)).thenReturn(List.of(tdInf));
                when(slotHorarioRepository.findAll()).thenReturn(List.of(slot1));
                when(salaRepository.findAll()).thenReturn(List.of(salaComum));
                when(aulaRepository.findByTurmaIdNot(1L)).thenReturn(List.of());
                when(disponibilidadeProfessorRepository.findByProfessorIdIn(eq(Set.of(12L)))).thenReturn(List.of(
                                disponibilidade(600L, profCarlos, slot1)));

                ScheduleGenerationResponseDTO resultado = scheduleGenerator
                                .gerarHorario(new ScheduleGenerationRequestDTO(1L));

                assertFalse(resultado.getSucesso());
                assertEquals(1, resultado.getTotalAulasNecessarias());
                assertEquals(0, resultado.getTotalAulasAlocadas());
                assertTrue(resultado.getObservacoes().stream()
                                .anyMatch(obs -> obs.contains("Nenhuma sala compativel")));
        }

        @Test
        void deveLancarBadRequestQuandoTurmaTemCapacidadeInvalida() {
                Turma turma = Turma.builder().id(1L).nome("9A").capacidade(0).build();
                when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> scheduleGenerator.gerarHorario(new ScheduleGenerationRequestDTO(1L)));

                assertEquals(400, exception.getStatusCode().value());
                assertTrue(exception.getReason().contains("capacidade invalida"));
        }

        @Test
        void deveLancarBadRequestQuandoTurmaDisciplinaTemCargaHorariaInvalida() {
                Turma turma = Turma.builder().id(1L).nome("9A").capacidade(30).build();
                Professor profJoao = Professor.builder().id(10L).nome("Joao").build();
                Disciplina matematica = Disciplina.builder().id(100L).nome("Matematica").requerLaboratorio(false)
                                .build();

                TurmaDisciplina tdInvalida = TurmaDisciplina.builder()
                                .id(1000L)
                                .turma(turma)
                                .professor(profJoao)
                                .disciplina(matematica)
                                .cargaHorariaSemanal(0)
                                .build();

                when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
                when(turmaDisciplinaRepository.findByTurmaId(1L)).thenReturn(List.of(tdInvalida));

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> scheduleGenerator.gerarHorario(new ScheduleGenerationRequestDTO(1L)));

                assertEquals(400, exception.getStatusCode().value());
                assertTrue(exception.getReason().contains("cargaHorariaSemanal invalida"));
        }

        @Test
        void deveRetornarParcialmenteQuandoHouverConflitoIntransponivel() {
                // Cenário: Turma precisa de 3 aulas, mas só há 2 slots no sistema para aquele
                // professor
                Turma turma = Turma.builder().id(1L).nome("9A").capacidade(30).build();
                Professor profJoao = Professor.builder().id(10L).nome("Joao").build();
                Disciplina matematica = Disciplina.builder().id(100L).nome("Matematica").requerLaboratorio(false)
                                .build();

                TurmaDisciplina tdMat = TurmaDisciplina.builder()
                                .id(1000L)
                                .turma(turma)
                                .professor(profJoao)
                                .disciplina(matematica)
                                .cargaHorariaSemanal(3) // 3 aulas exigidas!
                                .build();

                SlotHorario slot1 = slot(1L, DayOfWeek.MONDAY, "08:00", "09:00");
                SlotHorario slot2 = slot(2L, DayOfWeek.MONDAY, "09:00", "10:00");
                Sala sala = Sala.builder().id(20L).nome("Sala 101").capacidade(35).tipoSala(TipoSala.COMUM).build();

                when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
                when(turmaDisciplinaRepository.findByTurmaId(1L)).thenReturn(List.of(tdMat));
                when(slotHorarioRepository.findAll()).thenReturn(List.of(slot1, slot2));
                when(salaRepository.findAll()).thenReturn(List.of(sala));
                when(aulaRepository.findByTurmaIdNot(1L)).thenReturn(List.of());

                // Professor João só tem 2 disponibilidades, mas a matéria exige 3
                when(disponibilidadeProfessorRepository.findByProfessorIdIn(eq(Set.of(10L)))).thenReturn(List.of(
                                disponibilidade(500L, profJoao, slot1),
                                disponibilidade(501L, profJoao, slot2)));

                ScheduleGenerationResponseDTO resultado = scheduleGenerator
                                .gerarHorario(new ScheduleGenerationRequestDTO(1L));

                // Validação da resiliência do Fallback Parcial
                assertFalse(resultado.getSucesso()); // Falhou em gerar a grade completa de 3 aulas
                assertEquals(3, resultado.getTotalAulasNecessarias()); // Sabia que precisava de 3
                assertEquals(2, resultado.getTotalAulasAlocadas()); // Mas salvou o estado em 2!
                assertEquals(2, resultado.getAulas().size()); // Lista devolve as 2 já alocadas
                assertTrue(resultado.getObservacoes().stream()
                                .anyMatch(obs -> obs.contains("Nao foi encontrada solucao completa")));
        }

        private SlotHorario slot(Long id, DayOfWeek diaSemana, String inicio, String fim) {
                return SlotHorario.builder()
                                .id(id)
                                .diaSemana(diaSemana)
                                .horaInicio(LocalTime.parse(inicio))
                                .horaFim(LocalTime.parse(fim))
                                .build();
        }

        private DisponibilidadeProfessor disponibilidade(Long id, Professor professor, SlotHorario slot) {
                return DisponibilidadeProfessor.builder()
                                .id(id)
                                .professor(professor)
                                .slotHorario(slot)
                                .build();
        }
}
