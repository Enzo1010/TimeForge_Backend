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
public class DisponibilidadeProfessorRequestDTO {

    @NotNull(message = "professorId e obrigatorio")
    @Positive(message = "professorId deve ser maior que zero")
    private Long professorId;

    @NotNull(message = "slotHorarioId e obrigatorio")
    @Positive(message = "slotHorarioId deve ser maior que zero")
    private Long slotHorarioId;
}
