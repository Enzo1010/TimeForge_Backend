package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.TurmaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.TurmaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
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
class TurmaServiceTest {

    @Mock
    private TurmaRepository repository;

    @Mock
    private TurmaDisciplinaRepository turmaDisciplinaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @InjectMocks
    private TurmaService turmaService;

    private Turma turma(Long id, String nome, int capacidade) {
        return Turma.builder().id(id).nome(nome).capacidade(capacidade).build();
    }

    @Test
    void deveListarTodasTurmas() {
        when(repository.findAll()).thenReturn(List.of(
                turma(1L, "Turma A", 30),
                turma(2L, "Turma B", 25)
        ));

        List<TurmaResponseDTO> result = turmaService.listarTurmas();

        assertEquals(2, result.size());
        assertEquals("Turma A", result.get(0).getNome());
    }

    @Test
    void deveRetornarTurmaPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(turma(1L, "Turma A", 30)));

        TurmaResponseDTO result = turmaService.listarTurmaId(1L);

        assertEquals("Turma A", result.getNome());
        assertEquals(30, result.getCapacidade());
    }

    @Test
    void deveLancarNotFoundQuandoTurmaNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> turmaService.listarTurmaId(99L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deveCadastrarTurma() {
        TurmaRequestDTO dto = new TurmaRequestDTO();
        dto.setNome("Turma C");
        dto.setCapacidade(35);

        when(repository.save(any(Turma.class))).thenReturn(turma(1L, "Turma C", 35));

        TurmaResponseDTO result = turmaService.cadastrarTurma(dto);

        assertEquals("Turma C", result.getNome());
        assertEquals(35, result.getCapacidade());
    }

    @Test
    void deveEditarTurma() {
        Turma existente = turma(1L, "Turma A", 30);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(turma(1L, "Turma A+", 40));

        TurmaRequestDTO dto = new TurmaRequestDTO();
        dto.setNome("Turma A+");
        dto.setCapacidade(40);

        TurmaResponseDTO result = turmaService.editarTurma(1L, dto);

        assertEquals("Turma A+", result.getNome());
        assertEquals(40, result.getCapacidade());
    }

    @Test
    void deveDeletarTurmaSemVinculos() {
        Turma existente = turma(1L, "Turma A", 30);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(turmaDisciplinaRepository.existsByTurmaId(1L)).thenReturn(false);
        when(aulaRepository.existsByTurmaId(1L)).thenReturn(false);

        turmaService.deletarTurma(1L);

        verify(repository).delete(existente);
    }

    @Test
    void deveLancarConflictQuandoTurmaTemTurmaDisciplina() {
        when(repository.findById(1L)).thenReturn(Optional.of(turma(1L, "Turma A", 30)));
        when(turmaDisciplinaRepository.existsByTurmaId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> turmaService.deletarTurma(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }

    @Test
    void deveLancarConflictQuandoTurmaTemAulas() {
        when(repository.findById(1L)).thenReturn(Optional.of(turma(1L, "Turma A", 30)));
        when(turmaDisciplinaRepository.existsByTurmaId(1L)).thenReturn(false);
        when(aulaRepository.existsByTurmaId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> turmaService.deletarTurma(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }
}
