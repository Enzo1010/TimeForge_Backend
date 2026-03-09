package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaDisciplinaResponseDTO {

    private Long id;
    private Long turmaId;
    private String turmaNome;
    private Long disciplinaId;
    private String disciplinaNome;
    private Long professorId;
    private String professorNome;
    private Integer cargaHorariaSemanal;
    private Boolean disciplinaRequerLaboratorio;
}
