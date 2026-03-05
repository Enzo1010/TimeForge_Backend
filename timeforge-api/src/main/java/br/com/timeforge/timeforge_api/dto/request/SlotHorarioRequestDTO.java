package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotHorarioRequestDTO {

  @NotNull(message = "Dia da semana é obrigatório")
  private DayOfWeek diaSemana;

  @NotNull(message = "Hora de início é obrigatório")
  private LocalTime horaInicio;

  @NotNull(message = "Hora de fim é obrigatório")
  private LocalTime horaFim;
}