package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorDisciplinaRequestDTO {

    @NotNull(message = "professorId é obrigatório")
    @Positive(message = "professorId deve ser maior que zero")
    private Long professorId;

    @NotNull(message = "disciplinaId é obrigatório")
    @Positive(message = "disciplinaId deve ser maior que zero")
    private Long disciplinaId;
}
