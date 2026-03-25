package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "sala")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sala {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nome;

  private Integer capacidade;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sala", length = 20, nullable = true)
  @NotNull(message = "Tipo da sala e obrigatorio")
  @Builder.Default
  private TipoSala tipoSala = TipoSala.COMUM;
}
