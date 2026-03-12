package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
  @NotBlank(message = "Nome da sala e obrigatorio")
  private String nome;

  @NotNull(message = "Capacidade da sala e obrigatoria")
  @Positive(message = "Capacidade da sala deve ser maior que zero")
  private Integer capacidade;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sala", length = 20, nullable = true)
  @NotNull(message = "Tipo da sala e obrigatorio")
  @Builder.Default
  private TipoSala tipoSala = TipoSala.COMUM;
}
