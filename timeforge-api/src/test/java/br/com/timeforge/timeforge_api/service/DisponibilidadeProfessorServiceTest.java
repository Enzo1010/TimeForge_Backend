package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.DisponibilidadeProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.DisponibilidadeProfessorResponseDTO;
import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeProfessorServiceTest {

    @Mock
    private DisponibilidadeProfessorRepository disponibilidadeRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private SlotHorarioRepository slotHorarioRepository;

    @InjectMocks
    private DisponibilidadeProfessorService service;

    private Professor professor() {
        return Professor.builder().id(1L).nome("Ana").build();
    }

    private SlotHorario slot() {
        return SlotHorario.builder()
                .id(1L).diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(8, 0)).horaFim(LocalTime.of(9, 0))
                .build();
    }

    private DisponibilidadeProfessor disponibilidade() {
        return DisponibilidadeProfessor.builder()
                .id(1L).professor(professor()).slotHorario(slot())
                .build();
    }

    @Test
    void deveListarTodas() {
        when(disponibilidadeRepository.findAll()).thenReturn(List.of(disponibilidade()));

        List<DisponibilidadeProfessorResponseDTO> result = service.listarDisponibilidadesProfessor();

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getProfessorNome());
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDiaSemana());
    }

    @Test
    void deveRetornarPorId() {
        when(disponibilidadeRepository.findById(1L)).thenReturn(Optional.of(disponibilidade()));

        DisponibilidadeProfessorResponseDTO result = service.listarDisponibilidadeProfessorId(1L);

        assertEquals(1L, result.getId());
        assertEquals(LocalTime.of(8, 0), result.getHoraInicio());
    }

    @Test
    void deveLancarNotFoundQuandoNaoExiste() {
        when(disponibilidadeRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.listarDisponibilidadeProfessorId(99L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deveCadastrar() {
        DisponibilidadeProfessorRequestDTO dto = new DisponibilidadeProfessorRequestDTO();
        dto.setProfessorId(1L);
        dto.setSlotHorarioId(1L);

        when(disponibilidadeRepository.existsByProfessorIdAndSlotHorarioId(1L, 1L)).thenReturn(false);
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor()));
        when(slotHorarioRepository.findById(1L)).thenReturn(Optional.of(slot()));
        when(disponibilidadeRepository.save(any(DisponibilidadeProfessor.class))).thenReturn(disponibilidade());

        DisponibilidadeProfessorResponseDTO result = service.cadastrarDisponibilidadeProfessor(dto);

        assertEquals("Ana", result.getProfessorNome());
    }

    @Test
    void deveLancarConflictQuandoDuplicidadeNoCadastro() {
        DisponibilidadeProfessorRequestDTO dto = new DisponibilidadeProfessorRequestDTO();
        dto.setProfessorId(1L);
        dto.setSlotHorarioId(1L);

        when(disponibilidadeRepository.existsByProfessorIdAndSlotHorarioId(1L, 1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrarDisponibilidadeProfessor(dto)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(disponibilidadeRepository, never()).save(any());
    }

    @Test
    void deveLancarNotFoundQuandoProfessorNaoExiste() {
        DisponibilidadeProfessorRequestDTO dto = new DisponibilidadeProfessorRequestDTO();
        dto.setProfessorId(99L);
        dto.setSlotHorarioId(1L);

        when(disponibilidadeRepository.existsByProfessorIdAndSlotHorarioId(99L, 1L)).thenReturn(false);
        when(professorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrarDisponibilidadeProfessor(dto)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deveLancarNotFoundQuandoSlotNaoExiste() {
        DisponibilidadeProfessorRequestDTO dto = new DisponibilidadeProfessorRequestDTO();
        dto.setProfessorId(1L);
        dto.setSlotHorarioId(99L);

        when(disponibilidadeRepository.existsByProfessorIdAndSlotHorarioId(1L, 99L)).thenReturn(false);
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor()));
        when(slotHorarioRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrarDisponibilidadeProfessor(dto)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deveEditar() {
        DisponibilidadeProfessorRequestDTO dto = new DisponibilidadeProfessorRequestDTO();
        dto.setProfessorId(1L);
        dto.setSlotHorarioId(1L);

        DisponibilidadeProfessor existente = disponibilidade();
        when(disponibilidadeRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(disponibilidadeRepository.existsByProfessorIdAndSlotHorarioIdAndIdNot(1L, 1L, 1L)).thenReturn(false);
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor()));
        when(slotHorarioRepository.findById(1L)).thenReturn(Optional.of(slot()));
        when(disponibilidadeRepository.save(existente)).thenReturn(disponibilidade());

        DisponibilidadeProfessorResponseDTO result = service.editarDisponibilidadeProfessor(1L, dto);

        assertEquals("Ana", result.getProfessorNome());
    }

    @Test
    void deveDeletar() {
        DisponibilidadeProfessor existente = disponibilidade();
        when(disponibilidadeRepository.findById(1L)).thenReturn(Optional.of(existente));

        service.deletarDisponibilidadeProfessor(1L);

        verify(disponibilidadeRepository).delete(existente);
    }
}
