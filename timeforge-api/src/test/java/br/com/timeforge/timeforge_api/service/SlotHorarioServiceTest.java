package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.SlotHorarioRequestDTO;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SlotHorarioServiceTest {

    @Mock
    private SlotHorarioRepository slotHorarioRepository;

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
    }
}
