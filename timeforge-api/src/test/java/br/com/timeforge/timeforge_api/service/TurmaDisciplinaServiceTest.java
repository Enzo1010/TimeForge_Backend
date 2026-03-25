package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.TurmaDisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.TurmaDisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
import br.com.timeforge.timeforge_api.exception.DuplicateResourceException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurmaDisciplinaServiceTest {

    @Mock
    private TurmaDisciplinaRepository repository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @InjectMocks
    private TurmaDisciplinaService service;

    private Turma turma() {
        return Turma.builder().id(1L).nome("Turma A").capacidade(30).build();
    }

    private Disciplina disciplina() {
        return Disciplina.builder().id(1L).nome("Matematica").codigo("MAT01").requerLaboratorio(false).build();
    }

    private Professor professor() {
        return Professor.builder().id(1L).nome("Ana").build();
    }

    private TurmaDisciplina turmaDisciplina() {
        return TurmaDisciplina.builder()
                .id(1L)
                .turma(turma())
                .disciplina(disciplina())
                .professor(professor())
                .cargaHorariaSemanal(3)
                .build();
    }

    @Test
    void deveListarTodas() {
        when(repository.findAll()).thenReturn(List.of(turmaDisciplina()));

        List<TurmaDisciplinaResponseDTO> result = service.listarTurmasDisciplinas();

        assertEquals(1, result.size());
        assertEquals("Matematica", result.get(0).getDisciplinaNome());
        assertEquals("Ana", result.get(0).getProfessorNome());
        assertEquals(3, result.get(0).getCargaHorariaSemanal());
    }

    @Test
    void deveRetornarPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(turmaDisciplina()));

        TurmaDisciplinaResponseDTO result = service.listarTurmaDisciplinaId(1L);

        assertEquals(1L, result.getId());
        assertEquals("Turma A", result.getTurmaNome());
    }

    @Test
    void deveLancarNotFoundQuandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.listarTurmaDisciplinaId(99L)
        );

        assertEquals("TurmaDisciplina com id (99) nao encontrada!", ex.getMessage());
    }

    @Test
    void deveCadastrar() {
        TurmaDisciplinaRequestDTO dto = new TurmaDisciplinaRequestDTO();
        dto.setTurmaId(1L);
        dto.setDisciplinaId(1L);
        dto.setProfessorId(1L);
        dto.setCargaHorariaSemanal(3);

        when(repository.existsByTurmaIdAndDisciplinaId(1L, 1L)).thenReturn(false);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma()));
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina()));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor()));
        when(repository.save(any(TurmaDisciplina.class))).thenReturn(turmaDisciplina());

        TurmaDisciplinaResponseDTO result = service.cadastrarTurmaDisciplina(dto);

        assertEquals("Matematica", result.getDisciplinaNome());
    }

    @Test
    void deveLancarConflictQuandoDuplicidadeNoCadastro() {
        TurmaDisciplinaRequestDTO dto = new TurmaDisciplinaRequestDTO();
        dto.setTurmaId(1L);
        dto.setDisciplinaId(1L);
        dto.setProfessorId(1L);
        dto.setCargaHorariaSemanal(3);

        when(repository.existsByTurmaIdAndDisciplinaId(1L, 1L)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> service.cadastrarTurmaDisciplina(dto)
        );

        assertEquals("Ja existe vinculacao da turma (1) com disciplina (1).", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarNotFoundQuandoTurmaNaoExisteNoCadastro() {
        TurmaDisciplinaRequestDTO dto = new TurmaDisciplinaRequestDTO();
        dto.setTurmaId(99L);
        dto.setDisciplinaId(1L);
        dto.setProfessorId(1L);
        dto.setCargaHorariaSemanal(3);

        when(repository.existsByTurmaIdAndDisciplinaId(99L, 1L)).thenReturn(false);
        when(turmaRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.cadastrarTurmaDisciplina(dto)
        );

        assertEquals("Turma com id (99) nao encontrada!", ex.getMessage());
    }

    @Test
    void deveLancarNotFoundQuandoDisciplinaNaoExisteNoCadastro() {
        TurmaDisciplinaRequestDTO dto = new TurmaDisciplinaRequestDTO();
        dto.setTurmaId(1L);
        dto.setDisciplinaId(99L);
        dto.setProfessorId(1L);
        dto.setCargaHorariaSemanal(3);

        when(repository.existsByTurmaIdAndDisciplinaId(1L, 99L)).thenReturn(false);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma()));
        when(disciplinaRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.cadastrarTurmaDisciplina(dto)
        );

        assertEquals("Disciplina com id (99) nao encontrada!", ex.getMessage());
    }

    @Test
    void deveLancarNotFoundQuandoProfessorNaoExisteNoCadastro() {
        TurmaDisciplinaRequestDTO dto = new TurmaDisciplinaRequestDTO();
        dto.setTurmaId(1L);
        dto.setDisciplinaId(1L);
        dto.setProfessorId(99L);
        dto.setCargaHorariaSemanal(3);

        when(repository.existsByTurmaIdAndDisciplinaId(1L, 1L)).thenReturn(false);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma()));
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina()));
        when(professorRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.cadastrarTurmaDisciplina(dto)
        );

        assertEquals("Professor com id (99) nao encontrado!", ex.getMessage());
    }

    @Test
    void deveEditar() {
        TurmaDisciplinaRequestDTO dto = new TurmaDisciplinaRequestDTO();
        dto.setTurmaId(1L);
        dto.setDisciplinaId(1L);
        dto.setProfessorId(1L);
        dto.setCargaHorariaSemanal(5);

        TurmaDisciplina existente = turmaDisciplina();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByTurmaIdAndDisciplinaIdAndIdNot(1L, 1L, 1L)).thenReturn(false);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma()));
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina()));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor()));

        TurmaDisciplina editada = TurmaDisciplina.builder()
                .id(1L).turma(turma()).disciplina(disciplina()).professor(professor()).cargaHorariaSemanal(5).build();
        when(repository.save(existente)).thenReturn(editada);

        TurmaDisciplinaResponseDTO result = service.editarTurmaDisciplina(1L, dto);

        assertEquals(5, result.getCargaHorariaSemanal());
    }

    @Test
    void deveDeletar() {
        TurmaDisciplina existente = turmaDisciplina();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.deletarTurmaDisciplina(1L);

        verify(repository).delete(existente);
    }
}
