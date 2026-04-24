package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioSalasCapacidadeResponseDTO {

    private Integer totalSalas;
    private Integer capacidadeTotal;
    private Integer capacidadeMedia;

    @Builder.Default
    private List<RelatorioSalasCapacidadeItemDTO> itens = new ArrayList<>();
}
