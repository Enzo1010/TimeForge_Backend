package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado consolidado da geração de horários.
 * - sucesso: informa se a grade completa foi encontrada.
 * - mensagem: explica o resultado (completo, parcial ou sem solução).
 * - observacoes: pontos de diagnóstico úteis para depuração e apresentação.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleGenerationResponseDTO {

    private Boolean sucesso;
    private String mensagem;

    private Long turmaId;
    private String turmaNome;

    private Integer totalAulasNecessarias;
    private Integer totalAulasAlocadas;

    @Builder.Default
    private List<ScheduleAulaResponseDTO> aulas = new ArrayList<>();

    @Builder.Default
    private List<String> observacoes = new ArrayList<>();
}
