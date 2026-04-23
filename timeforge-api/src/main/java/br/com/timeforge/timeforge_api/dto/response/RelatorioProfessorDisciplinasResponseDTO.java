package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioProfessorDisciplinasResponseDTO {

    private Integer totalProfessores;
    private Integer totalDisciplinasVinculadas;
    private List<RelatorioProfessorDisciplinasItemDTO> itens;
}
