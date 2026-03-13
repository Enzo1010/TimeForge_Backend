package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ProfessorResponseDTO;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    private ProfessorRepository repository;

    @Mock
    private TurmaDisciplinaRepository turmaDisciplinaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;

    @InjectMocks
    private ProfessorService professorService;

    private Professor professor(Long id, String nome) {
        return Professor.builder().id(id).nome(nome).build();
    }

    @Test
    void deveListarTodosProfessores() {
        when(repository.findAll()).thenReturn(List.of(
                professor(1L, "Ana"),
                professor(2L, "Bruno")
        ));

        List<ProfessorResponseDTO> result = professorService.listarProfessores();

        assertEquals(2, result.size());
        assertEquals("Ana", result.get(0).getNome());
        assertEquals("Bruno", result.get(1).getNome());
    }

    @Test
    void deveRetornarProfessorPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(professor(1L, "Ana")));

        ProfessorResponseDTO result = professorService.listarProfessorId(1L);

        assertEquals(1L, result.getId());
        assertEquals("Ana", result.getNome());
    }

    @Test
    void deveLancarNotFoundQuandoProfessorNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.listarProfessorId(99L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deveCadastrarProfessor() {
        ProfessorRequestDTO dto = new ProfessorRequestDTO();
        dto.setNome("Carlos");

        when(repository.save(any(Professor.class))).thenReturn(professor(1L, "Carlos"));

        ProfessorResponseDTO result = professorService.cadastrarProfessor(dto);

        assertEquals(1L, result.getId());
        assertEquals("Carlos", result.getNome());
    }

    @Test
    void deveEditarProfessor() {
        Professor existente = professor(1L, "Ana");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(professor(1L, "Ana Maria"));

        ProfessorRequestDTO dto = new ProfessorRequestDTO();
        dto.setNome("Ana Maria");

        ProfessorResponseDTO result = professorService.editarProfessor(1L, dto);

        assertEquals("Ana Maria", result.getNome());
    }

    @Test
    void deveDeletarProfessorSemVinculos() {
        Professor existente = professor(1L, "Ana");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(turmaDisciplinaRepository.existsByProfessorId(1L)).thenReturn(false);
        when(disponibilidadeProfessorRepository.existsByProfessorId(1L)).thenReturn(false);
        when(aulaRepository.existsByProfessorId(1L)).thenReturn(false);

        professorService.deletarProfessor(1L);

        verify(repository).delete(existente);
    }

    @Test
    void deveLancarConflictQuandoProfessorTemTurmaDisciplina() {
        when(repository.findById(1L)).thenReturn(Optional.of(professor(1L, "Ana")));
        when(turmaDisciplinaRepository.existsByProfessorId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.deletarProfessor(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }

    @Test
    void deveLancarConflictQuandoProfessorTemDisponibilidade() {
        when(repository.findById(1L)).thenReturn(Optional.of(professor(1L, "Ana")));
        when(turmaDisciplinaRepository.existsByProfessorId(1L)).thenReturn(false);
        when(disponibilidadeProfessorRepository.existsByProfessorId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.deletarProfessor(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }

    @Test
    void deveLancarConflictQuandoProfessorTemAulas() {
        when(repository.findById(1L)).thenReturn(Optional.of(professor(1L, "Ana")));
        when(turmaDisciplinaRepository.existsByProfessorId(1L)).thenReturn(false);
        when(disponibilidadeProfessorRepository.existsByProfessorId(1L)).thenReturn(false);
        when(aulaRepository.existsByProfessorId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> professorService.deletarProfessor(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }
}
