package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotHorarioRequestDTO {

  @NotNull(message = "Dia da semana e obrigatorio")
  private DayOfWeek diaSemana;

  @NotNull(message = "Hora de inicio e obrigatorio")
  private LocalTime horaInicio;

  @NotNull(message = "Hora de fim e obrigatorio")
  private LocalTime horaFim;

  @AssertTrue(message = "horaInicio deve ser anterior a horaFim")
  public boolean isIntervaloHorarioValido() {
    if (horaInicio == null || horaFim == null) {
      return true;
    }
    return horaInicio.isBefore(horaFim);
  }
}
