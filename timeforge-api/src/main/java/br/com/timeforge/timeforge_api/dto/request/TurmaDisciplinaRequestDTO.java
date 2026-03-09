package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaDisciplinaRequestDTO {

    @NotNull(message = "turmaId e obrigatorio")
    @Positive(message = "turmaId deve ser maior que zero")
    private Long turmaId;

    @NotNull(message = "disciplinaId e obrigatorio")
    @Positive(message = "disciplinaId deve ser maior que zero")
    private Long disciplinaId;

    @NotNull(message = "professorId e obrigatorio")
    @Positive(message = "professorId deve ser maior que zero")
    private Long professorId;

    @NotNull(message = "cargaHorariaSemanal e obrigatoria")
    @Positive(message = "cargaHorariaSemanal deve ser maior que zero")
    private Integer cargaHorariaSemanal;
}
