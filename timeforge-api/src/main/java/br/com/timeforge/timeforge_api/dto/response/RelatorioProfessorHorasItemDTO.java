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
public class RelatorioProfessorHorasItemDTO {

    private Long professorId;
    private String professorNome;
    private Integer totalAulas;
    private Integer totalMinutos;
    private String totalHorasFormatadas;
}
