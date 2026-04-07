package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ConfiguracaoGeradorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ConfiguracaoGeradorResponseDTO;
import br.com.timeforge.timeforge_api.entity.ConfiguracaoGerador;
import br.com.timeforge.timeforge_api.repository.ConfiguracaoGeradorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoGeradorServiceTest {

    @Mock
    private ConfiguracaoGeradorRepository repository;

    @InjectMocks
    private ConfiguracaoGeradorService service;

    @Test
    void deveBuscarConfiguracaoExistente() {
        ConfiguracaoGerador existente = ConfiguracaoGerador.builder()
                .id(1L)
                .limiteIteracoes(180000)
                .timeoutMs(9000)
                .maxAulasProfessorDia(7)
                .maxAulasTurmaDia(5)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        ConfiguracaoGeradorResponseDTO response = service.buscarConfiguracao();

        assertEquals(180000, response.getLimiteIteracoes());
        assertEquals(9000, response.getTimeoutMs());
        assertEquals(7, response.getMaxAulasProfessorDia());
        assertEquals(5, response.getMaxAulasTurmaDia());
    }

    @Test
    void deveCriarConfiguracaoPadraoQuandoNaoExistir() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(any(ConfiguracaoGerador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfiguracaoGeradorResponseDTO response = service.buscarConfiguracao();

        assertEquals(200000, response.getLimiteIteracoes());
        assertEquals(10000, response.getTimeoutMs());
        assertEquals(8, response.getMaxAulasProfessorDia());
        assertEquals(6, response.getMaxAulasTurmaDia());
    }

    @Test
    void deveAtualizarConfiguracaoExistente() {
        ConfiguracaoGerador existente = ConfiguracaoGerador.builder()
                .id(1L)
                .limiteIteracoes(200000)
                .timeoutMs(10000)
                .maxAulasProfessorDia(8)
                .maxAulasTurmaDia(6)
                .build();

        ConfiguracaoGeradorRequestDTO request = new ConfiguracaoGeradorRequestDTO(
                240000,
                12000,
                9,
                7
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(ConfiguracaoGerador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfiguracaoGeradorResponseDTO response = service.atualizarConfiguracao(request);

        ArgumentCaptor<ConfiguracaoGerador> captor = ArgumentCaptor.forClass(ConfiguracaoGerador.class);
        verify(repository).save(captor.capture());

        assertEquals(240000, captor.getValue().getLimiteIteracoes());
        assertEquals(12000, captor.getValue().getTimeoutMs());
        assertEquals(9, captor.getValue().getMaxAulasProfessorDia());
        assertEquals(7, captor.getValue().getMaxAulasTurmaDia());

        assertEquals(240000, response.getLimiteIteracoes());
        assertEquals(12000, response.getTimeoutMs());
        assertEquals(9, response.getMaxAulasProfessorDia());
        assertEquals(7, response.getMaxAulasTurmaDia());
    }
}
