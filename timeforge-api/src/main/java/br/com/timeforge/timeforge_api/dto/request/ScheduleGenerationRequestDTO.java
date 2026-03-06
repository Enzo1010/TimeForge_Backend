package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotNull;
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
    private Long turmaId;
}
