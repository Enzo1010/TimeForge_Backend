package br.com.timeforge.timeforge_api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "aula",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sala_slot", columnNames = {"sala_id", "slot_horario_id"}),
                @UniqueConstraint(name = "uk_turma_slot", columnNames = {"turma_id", "slot_horario_id"}),
                @UniqueConstraint(name = "uk_prof_slot", columnNames = {"professor_id", "slot_horario_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aula {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "disciplina_id", nullable = false)
  private Disciplina disciplina;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "professor_id", nullable = false)
  private Professor professor;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "turma_id", nullable = false)
  private Turma turma;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "sala_id", nullable = false)
  private Sala sala;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "slot_horario_id", nullable = false)
  private SlotHorario slotHorario;
}