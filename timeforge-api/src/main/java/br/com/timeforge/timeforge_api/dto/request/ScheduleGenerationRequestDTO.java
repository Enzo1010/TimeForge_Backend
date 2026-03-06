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
public class ScheduleGenerationRequestDTO {

    @NotNull(message = "turmaId e obrigatorio")
    @Positive(message = "turmaId deve ser maior que zero")
    private Long turmaId;
}
