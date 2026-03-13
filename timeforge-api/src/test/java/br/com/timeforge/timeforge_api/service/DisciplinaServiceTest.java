package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.DisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.DisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
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
class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository repository;

    @Mock
    private TurmaDisciplinaRepository turmaDisciplinaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @InjectMocks
    private DisciplinaService disciplinaService;

    private Disciplina disciplina(Long id, String nome, String codigo, Boolean requerLab) {
        return Disciplina.builder().id(id).nome(nome).codigo(codigo).requerLaboratorio(requerLab).build();
    }

    @Test
    void deveListarTodasDisciplinas() {
        when(repository.findAll()).thenReturn(List.of(
                disciplina(1L, "Matematica", "MAT01", false),
                disciplina(2L, "Quimica", "QUI01", true)
        ));

        List<DisciplinaResponseDTO> result = disciplinaService.listarDisciplinas();

        assertEquals(2, result.size());
        assertEquals("Matematica", result.get(0).getNome());
        assertEquals(true, result.get(1).getRequerLaboratorio());
    }

    @Test
    void deveRetornarDisciplinaPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(disciplina(1L, "Matematica", "MAT01", false)));

        DisciplinaResponseDTO result = disciplinaService.listarDisciplinaId(1L);

        assertEquals("Matematica", result.getNome());
        assertEquals("MAT01", result.getCodigo());
    }

    @Test
    void deveLancarNotFoundQuandoDisciplinaNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> disciplinaService.listarDisciplinaId(99L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deveCadastrarDisciplina() {
        DisciplinaRequestDTO dto = new DisciplinaRequestDTO();
        dto.setNome("Fisica");
        dto.setCodigo("FIS01");
        dto.setRequerLaboratorio(true);

        when(repository.existsByCodigo("FIS01")).thenReturn(false);
        when(repository.save(any(Disciplina.class))).thenReturn(disciplina(1L, "Fisica", "FIS01", true));

        DisciplinaResponseDTO result = disciplinaService.cadastrarDisciplina(dto);

        assertEquals("Fisica", result.getNome());
        assertEquals("FIS01", result.getCodigo());
        assertEquals(true, result.getRequerLaboratorio());
    }

    @Test
    void deveLancarConflictQuandoCodigoDuplicadoNoCadastro() {
        DisciplinaRequestDTO dto = new DisciplinaRequestDTO();
        dto.setNome("Fisica");
        dto.setCodigo("FIS01");

        when(repository.existsByCodigo("FIS01")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> disciplinaService.cadastrarDisciplina(dto)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void deveCadastrarDisciplinaSemCodigo() {
        DisciplinaRequestDTO dto = new DisciplinaRequestDTO();
        dto.setNome("Historia");
        dto.setCodigo(null);
        dto.setRequerLaboratorio(false);

        when(repository.save(any(Disciplina.class))).thenReturn(disciplina(1L, "Historia", null, false));

        DisciplinaResponseDTO result = disciplinaService.cadastrarDisciplina(dto);

        assertEquals("Historia", result.getNome());
    }

    @Test
    void deveEditarDisciplina() {
        Disciplina existente = disciplina(1L, "Matematica", "MAT01", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByCodigoAndIdNot("MAT02", 1L)).thenReturn(false);
        when(repository.save(existente)).thenReturn(disciplina(1L, "Matematica II", "MAT02", false));

        DisciplinaRequestDTO dto = new DisciplinaRequestDTO();
        dto.setNome("Matematica II");
        dto.setCodigo("MAT02");
        dto.setRequerLaboratorio(false);

        DisciplinaResponseDTO result = disciplinaService.editarDisciplina(1L, dto);

        assertEquals("Matematica II", result.getNome());
        assertEquals("MAT02", result.getCodigo());
    }

    @Test
    void deveLancarConflictQuandoCodigoDuplicadoNaEdicao() {
        Disciplina existente = disciplina(1L, "Matematica", "MAT01", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByCodigoAndIdNot("FIS01", 1L)).thenReturn(true);

        DisciplinaRequestDTO dto = new DisciplinaRequestDTO();
        dto.setNome("Matematica");
        dto.setCodigo("FIS01");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> disciplinaService.editarDisciplina(1L, dto)
        );

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void deveDeletarDisciplinaSemVinculos() {
        Disciplina existente = disciplina(1L, "Matematica", "MAT01", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(turmaDisciplinaRepository.existsByDisciplinaId(1L)).thenReturn(false);
        when(aulaRepository.existsByDisciplinaId(1L)).thenReturn(false);

        disciplinaService.deletarDisciplina(1L);

        verify(repository).delete(existente);
    }

    @Test
    void deveLancarConflictQuandoDisciplinaTemTurmaDisciplina() {
        when(repository.findById(1L)).thenReturn(Optional.of(disciplina(1L, "Matematica", "MAT01", false)));
        when(turmaDisciplinaRepository.existsByDisciplinaId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> disciplinaService.deletarDisciplina(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }

    @Test
    void deveLancarConflictQuandoDisciplinaTemAulas() {
        when(repository.findById(1L)).thenReturn(Optional.of(disciplina(1L, "Matematica", "MAT01", false)));
        when(turmaDisciplinaRepository.existsByDisciplinaId(1L)).thenReturn(false);
        when(aulaRepository.existsByDisciplinaId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> disciplinaService.deletarDisciplina(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }
}
