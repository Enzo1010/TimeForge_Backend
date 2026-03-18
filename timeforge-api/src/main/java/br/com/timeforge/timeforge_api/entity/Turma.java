package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
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
  private String nome;

  private Integer capacidade;
}
