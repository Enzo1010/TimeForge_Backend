package br.com.timeforge.timeforge_api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorDisciplinaResponseDTO {

    private Long id;
    private Long professorId;
    private String professorNome;
    private Long disciplinaId;
    private String disciplinaNome;
    private String disciplinaCodigo;
}
