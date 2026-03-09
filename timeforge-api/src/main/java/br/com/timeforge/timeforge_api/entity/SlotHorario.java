package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "slot_horario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotHorario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @NotNull(message = "Dia da semana e obrigatorio")
  private DayOfWeek diaSemana;

  @Column(nullable = false)
  @NotNull(message = "Hora de inicio e obrigatorio")
  private LocalTime horaInicio;

  @Column(nullable = false)
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
