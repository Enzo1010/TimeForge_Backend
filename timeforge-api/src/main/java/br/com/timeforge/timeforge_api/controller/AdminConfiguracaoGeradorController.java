package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.ConfiguracaoGeradorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ConfiguracaoGeradorResponseDTO;
import br.com.timeforge.timeforge_api.service.ConfiguracaoGeradorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/configuracoes/gerador")
public class AdminConfiguracaoGeradorController {

    private final ConfiguracaoGeradorService configuracaoGeradorService;

    public AdminConfiguracaoGeradorController(ConfiguracaoGeradorService configuracaoGeradorService) {
        this.configuracaoGeradorService = configuracaoGeradorService;
    }

    @GetMapping
    public ConfiguracaoGeradorResponseDTO buscarConfiguracao() {
        return configuracaoGeradorService.buscarConfiguracao();
    }

    @PutMapping
    public ConfiguracaoGeradorResponseDTO atualizarConfiguracao(
            @RequestBody @Valid ConfiguracaoGeradorRequestDTO payload
    ) {
        return configuracaoGeradorService.atualizarConfiguracao(payload);
    }
}
