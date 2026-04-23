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
public class RelatorioProfessorHorasResponseDTO {

    private Integer totalProfessores;
    private Integer totalAulas;
    private Integer totalMinutos;
    private String totalHorasFormatadas;

    @Builder.Default
    private List<RelatorioProfessorHorasItemDTO> itens = new ArrayList<>();
}
