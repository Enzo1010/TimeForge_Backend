package br.com.timeforge.timeforge_api.dto.response;

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
public class SlotHorarioResponseDTO {
  private Long id;
  private DayOfWeek diaSemana;
  private LocalTime horaInicio;
  private LocalTime horaFim;
}