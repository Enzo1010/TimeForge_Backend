package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(
        name = "turma_disciplina",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_turma_disciplina",
                columnNames = {"turma_id", "disciplina_id"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TurmaDisciplina {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "turma_id", nullable = false)
  @NotNull(message = "Turma e obrigatoria")
  private Turma turma;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "disciplina_id", nullable = false)
  @NotNull(message = "Disciplina e obrigatoria")
  private Disciplina disciplina;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "professor_id", nullable = false)
  @NotNull(message = "Professor e obrigatorio")
  private Professor professor;

  @Column(name = "carga_horaria_semanal", nullable = false)
  @NotNull(message = "Carga horaria semanal e obrigatoria")
  @Positive(message = "Carga horaria semanal deve ser maior que zero")
  private Integer cargaHorariaSemanal;
}
