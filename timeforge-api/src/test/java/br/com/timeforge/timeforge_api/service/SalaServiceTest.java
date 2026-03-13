package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.SalaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.SalaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.TipoSala;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
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
class SalaServiceTest {

    @Mock
    private SalaRepository repository;

    @Mock
    private AulaRepository aulaRepository;

    @InjectMocks
    private SalaService salaService;

    private Sala sala(Long id, String nome, int capacidade, TipoSala tipo) {
        return Sala.builder().id(id).nome(nome).capacidade(capacidade).tipoSala(tipo).build();
    }

    @Test
    void deveListarTodasSalas() {
        when(repository.findAll()).thenReturn(List.of(
                sala(1L, "Sala 101", 40, TipoSala.COMUM),
                sala(2L, "Lab Info", 30, TipoSala.LABORATORIO)
        ));

        List<SalaResponseDTO> result = salaService.listarSalas();

        assertEquals(2, result.size());
        assertEquals("Sala 101", result.get(0).getNome());
        assertEquals(TipoSala.LABORATORIO, result.get(1).getTipoSala());
    }

    @Test
    void deveRetornarSalaPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(sala(1L, "Sala 101", 40, TipoSala.COMUM)));

        SalaResponseDTO result = salaService.listarSalaId(1L);

        assertEquals("Sala 101", result.getNome());
        assertEquals(40, result.getCapacidade());
    }

    @Test
    void deveLancarNotFoundQuandoSalaNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> salaService.listarSalaId(99L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void deveCadastrarSala() {
        SalaRequestDTO dto = new SalaRequestDTO();
        dto.setNome("Sala 201");
        dto.setCapacidade(50);
        dto.setTipoSala(TipoSala.COMUM);

        when(repository.save(any(Sala.class))).thenReturn(sala(1L, "Sala 201", 50, TipoSala.COMUM));

        SalaResponseDTO result = salaService.cadastrarSala(dto);

        assertEquals("Sala 201", result.getNome());
        assertEquals(50, result.getCapacidade());
    }

    @Test
    void deveEditarSala() {
        Sala existente = sala(1L, "Sala 101", 40, TipoSala.COMUM);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(sala(1L, "Sala 101 Reformada", 45, TipoSala.LABORATORIO));

        SalaRequestDTO dto = new SalaRequestDTO();
        dto.setNome("Sala 101 Reformada");
        dto.setCapacidade(45);
        dto.setTipoSala(TipoSala.LABORATORIO);

        SalaResponseDTO result = salaService.editarSala(1L, dto);

        assertEquals("Sala 101 Reformada", result.getNome());
        assertEquals(45, result.getCapacidade());
        assertEquals(TipoSala.LABORATORIO, result.getTipoSala());
    }

    @Test
    void deveDeletarSalaSemVinculos() {
        Sala existente = sala(1L, "Sala 101", 40, TipoSala.COMUM);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(aulaRepository.existsBySalaId(1L)).thenReturn(false);

        salaService.deletarSala(1L);

        verify(repository).delete(existente);
    }

    @Test
    void deveLancarConflictQuandoSalaTemAulas() {
        when(repository.findById(1L)).thenReturn(Optional.of(sala(1L, "Sala 101", 40, TipoSala.COMUM)));
        when(aulaRepository.existsBySalaId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> salaService.deletarSala(1L)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(repository, never()).delete(any());
    }
}
