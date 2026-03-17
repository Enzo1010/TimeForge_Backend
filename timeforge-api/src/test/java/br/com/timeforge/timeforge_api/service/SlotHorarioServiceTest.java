package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.SlotHorarioRequestDTO;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotHorarioServiceTest {

    @Mock
    private SlotHorarioRepository slotHorarioRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;

    @InjectMocks
    private SlotHorarioService slotHorarioService;

    @Test
    void deveLancarBadRequestQuandoHoraInicioNaoEhAnteriorAHoraFim() {
        SlotHorarioRequestDTO payload = new SlotHorarioRequestDTO(
                DayOfWeek.MONDAY,
                LocalTime.parse("10:00"),
                LocalTime.parse("09:00")
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> slotHorarioService.gravarSlotHorario(payload)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("horaInicio deve ser anterior a horaFim.", exception.getReason());
        verifyNoInteractions(slotHorarioRepository);
        verifyNoInteractions(aulaRepository);
        verifyNoInteractions(disponibilidadeProfessorRepository);
    }

    @Test
    void deveLancarConflictQuandoCadastroTemSobreposicaoComOutroSlotDoMesmoDia() {
        SlotHorarioRequestDTO payload = new SlotHorarioRequestDTO(
                DayOfWeek.MONDAY,
                LocalTime.parse("08:30"),
                LocalTime.parse("09:30")
        );

        when(slotHorarioRepository.existsByDiaSemanaAndHoraInicioLessThanAndHoraFimGreaterThan(
                DayOfWeek.MONDAY,
                LocalTime.parse("09:30"),
                LocalTime.parse("08:30")
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> slotHorarioService.gravarSlotHorario(payload)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Ja existe slot sobreposto para o dia informado.", exception.getReason());
    }

    @Test
    void deveLancarConflictQuandoEdicaoTemSobreposicaoComOutroSlotDoMesmoDia() {
        SlotHorarioRequestDTO payload = new SlotHorarioRequestDTO(
                DayOfWeek.MONDAY,
                LocalTime.parse("08:30"),
                LocalTime.parse("09:30")
        );
        SlotHorario slotExistente = SlotHorario.builder()
                .id(10L)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.parse("07:00"))
                .horaFim(LocalTime.parse("08:00"))
                .build();

        when(slotHorarioRepository.findById(10L)).thenReturn(Optional.of(slotExistente));

        when(slotHorarioRepository.existsByDiaSemanaAndHoraInicioLessThanAndHoraFimGreaterThanAndIdNot(
                DayOfWeek.MONDAY,
                LocalTime.parse("09:30"),
                LocalTime.parse("08:30"),
                10L
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> slotHorarioService.editarSlotHorario(10L, payload)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Ja existe slot sobreposto para o dia informado.", exception.getReason());
    }

    @Test
    void deveLancarNotFoundNaEdicaoQuandoIdNaoExisteAntesDeValidarSobreposicao() {
        SlotHorarioRequestDTO payload = new SlotHorarioRequestDTO(
                DayOfWeek.MONDAY,
                LocalTime.parse("08:30"),
                LocalTime.parse("09:30")
        );

        when(slotHorarioRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> slotHorarioService.editarSlotHorario(999L, payload)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(slotHorarioRepository).findById(999L);
        verify(slotHorarioRepository, never())
                .existsByDiaSemanaAndHoraInicioLessThanAndHoraFimGreaterThanAndIdNot(any(), any(), any(), anyLong());
    }

    @Test
    void deveLancarConflictQuandoSlotPossuiDisponibilidadeVinculada() {
        SlotHorario slot = SlotHorario.builder()
                .id(1L)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.parse("08:00"))
                .horaFim(LocalTime.parse("09:00"))
                .build();

        when(slotHorarioRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(disponibilidadeProfessorRepository.existsBySlotHorarioId(1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> slotHorarioService.excluirSlotHorario(1L)
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(slotHorarioRepository).findById(1L);
    }

    @Test
    void deveLancarConflictQuandoSlotPossuiAulaVinculada() {
        SlotHorario slot = SlotHorario.builder()
                .id(2L)
                .diaSemana(DayOfWeek.TUESDAY)
                .horaInicio(LocalTime.parse("10:00"))
                .horaFim(LocalTime.parse("11:00"))
                .build();

        when(slotHorarioRepository.findById(2L)).thenReturn(Optional.of(slot));
        when(disponibilidadeProfessorRepository.existsBySlotHorarioId(2L)).thenReturn(false);
        when(aulaRepository.existsBySlotHorarioId(2L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> slotHorarioService.excluirSlotHorario(2L)
        );

        assertEquals(409, exception.getStatusCode().value());
    }
}
