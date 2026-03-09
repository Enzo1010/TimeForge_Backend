package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaRequestDTO {

  @NotBlank(message = "Nome da turma e obrigatorio")
  private String nome;

  @NotNull(message = "Capacidade da turma e obrigatoria")
  @Positive(message = "Capacidade da turma deve ser maior que zero")
  private Integer capacidade;
}
