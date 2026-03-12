package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
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
  private Turma turma;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "disciplina_id", nullable = false)
  private Disciplina disciplina;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "professor_id", nullable = false)
  private Professor professor;

  @Column(name = "carga_horaria_semanal", nullable = false)
  private Integer cargaHorariaSemanal;
}
