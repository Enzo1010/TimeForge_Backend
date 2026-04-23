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
public class RelatorioProfessorDisciplinasItemDTO {

    private Long professorId;
    private String professorNome;
    private Integer totalDisciplinas;
    private List<String> disciplinas;
}
