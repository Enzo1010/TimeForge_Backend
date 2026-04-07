package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoGeradorResponseDTO {

    private Integer limiteIteracoes;
    private Integer timeoutMs;
    private Integer maxAulasProfessorDia;
    private Integer maxAulasTurmaDia;
}
