package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "professor_disciplina",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_professor_disciplina",
                columnNames = {"professor_id", "disciplina_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessorDisciplina {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "professor_id", nullable = false)
  private Professor professor;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "disciplina_id", nullable = false)
  private Disciplina disciplina;
}
