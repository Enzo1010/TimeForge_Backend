package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioSalasCapacidadeItemDTO {

    private Long salaId;
    private String salaNome;
    private Integer capacidade;
    private String tipoSala;
}
