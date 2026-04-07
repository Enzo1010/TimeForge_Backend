package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoGeradorRequestDTO {

    @NotNull(message = "limiteIteracoes e obrigatorio")
    @Positive(message = "limiteIteracoes deve ser maior que zero")
    private Integer limiteIteracoes;

    @NotNull(message = "timeoutMs e obrigatorio")
    @Positive(message = "timeoutMs deve ser maior que zero")
    private Integer timeoutMs;

    @NotNull(message = "maxAulasProfessorDia e obrigatorio")
    @Positive(message = "maxAulasProfessorDia deve ser maior que zero")
    private Integer maxAulasProfessorDia;

    @NotNull(message = "maxAulasTurmaDia e obrigatorio")
    @Positive(message = "maxAulasTurmaDia deve ser maior que zero")
    private Integer maxAulasTurmaDia;
}
