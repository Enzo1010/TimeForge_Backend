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
public class ScheduleTurmaResponseDTO {

    private Long turmaId;
    private String turmaNome;
    private Integer totalAulas;

    @Builder.Default
    private List<ScheduleAulaResponseDTO> aulas = new ArrayList<>();
}
