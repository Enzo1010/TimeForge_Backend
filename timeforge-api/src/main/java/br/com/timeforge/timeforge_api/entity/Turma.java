package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "turma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turma {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Nome da turma e obrigatorio")
  private String nome;

  @NotNull(message = "Capacidade da turma e obrigatoria")
  @Positive(message = "Capacidade da turma deve ser maior que zero")
  private Integer capacidade;
}
