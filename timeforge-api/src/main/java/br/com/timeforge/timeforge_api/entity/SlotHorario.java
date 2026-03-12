package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
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
  private DayOfWeek diaSemana;

  @Column(nullable = false)
  private LocalTime horaInicio;

  @Column(nullable = false)
  private LocalTime horaFim;
}
