package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ConfiguracaoGeradorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ConfiguracaoGeradorResponseDTO;
import br.com.timeforge.timeforge_api.entity.ConfiguracaoGerador;
import br.com.timeforge.timeforge_api.repository.ConfiguracaoGeradorRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracaoGeradorService {

    private static final Long CONFIG_ID = 1L;
    private static final int DEFAULT_LIMITE_ITERACOES = 200_000;
    private static final int DEFAULT_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_MAX_AULAS_PROFESSOR_DIA = 8;
    private static final int DEFAULT_MAX_AULAS_TURMA_DIA = 6;

    private final ConfiguracaoGeradorRepository repository;

    public ConfiguracaoGeradorService(ConfiguracaoGeradorRepository repository) {
        this.repository = repository;
    }

    public ConfiguracaoGeradorResponseDTO buscarConfiguracao() {
        return toResponse(obterOuCriarConfiguracao());
    }

    public ConfiguracaoGeradorResponseDTO atualizarConfiguracao(ConfiguracaoGeradorRequestDTO payload) {
        ConfiguracaoGerador configuracao = obterOuCriarConfiguracao();

        configuracao.setLimiteIteracoes(payload.getLimiteIteracoes());
        configuracao.setTimeoutMs(payload.getTimeoutMs());
        configuracao.setMaxAulasProfessorDia(payload.getMaxAulasProfessorDia());
        configuracao.setMaxAulasTurmaDia(payload.getMaxAulasTurmaDia());

        ConfiguracaoGerador salvo = repository.save(configuracao);
        return toResponse(salvo);
    }

    private ConfiguracaoGerador obterOuCriarConfiguracao() {
        return repository.findById(CONFIG_ID).orElseGet(this::criarConfiguracaoPadrao);
    }

    private ConfiguracaoGerador criarConfiguracaoPadrao() {
        ConfiguracaoGerador configuracao = ConfiguracaoGerador.builder()
                .id(CONFIG_ID)
                .limiteIteracoes(DEFAULT_LIMITE_ITERACOES)
                .timeoutMs(DEFAULT_TIMEOUT_MS)
                .maxAulasProfessorDia(DEFAULT_MAX_AULAS_PROFESSOR_DIA)
                .maxAulasTurmaDia(DEFAULT_MAX_AULAS_TURMA_DIA)
                .build();

        return repository.save(configuracao);
    }

    private ConfiguracaoGeradorResponseDTO toResponse(ConfiguracaoGerador entity) {
        return ConfiguracaoGeradorResponseDTO.builder()
                .limiteIteracoes(entity.getLimiteIteracoes())
                .timeoutMs(entity.getTimeoutMs())
                .maxAulasProfessorDia(entity.getMaxAulasProfessorDia())
                .maxAulasTurmaDia(entity.getMaxAulasTurmaDia())
                .build();
    }
}
