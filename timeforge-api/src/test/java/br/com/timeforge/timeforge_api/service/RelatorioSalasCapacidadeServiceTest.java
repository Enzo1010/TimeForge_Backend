package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.RelatorioSalasCapacidadeResponseDTO;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.TipoSala;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioSalasCapacidadeServiceTest {

    @Mock
    private SalaRepository salaRepository;

    @InjectMocks
    private RelatorioSalasCapacidadeService relatorioSalasCapacidadeService;

    @Test
    void deveGerarRelatorioDeSalasComCapacidades() {
        when(salaRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"))).thenReturn(List.of(
                Sala.builder().id(2L).nome("Lab 01").capacidade(30).tipoSala(TipoSala.LABORATORIO).build(),
                Sala.builder().id(1L).nome("Sala 101").capacidade(40).tipoSala(TipoSala.COMUM).build()
        ));

        RelatorioSalasCapacidadeResponseDTO result = relatorioSalasCapacidadeService.gerarRelatorio();

        assertEquals(2, result.getTotalSalas());
        assertEquals(70, result.getCapacidadeTotal());
        assertEquals(35, result.getCapacidadeMedia());
        assertEquals("Lab 01", result.getItens().get(0).getSalaNome());
        assertEquals("Laboratorio", result.getItens().get(0).getTipoSala());
        assertEquals("Comum", result.getItens().get(1).getTipoSala());
        verify(salaRepository).findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }
}
