package br.com.timeforge.timeforge_api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "disponibilidade_professor",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_professor_slot",
                columnNames = {"professor_id", "slot_horario_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadeProfessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_horario_id", nullable = false)
    private SlotHorario slotHorario;
}