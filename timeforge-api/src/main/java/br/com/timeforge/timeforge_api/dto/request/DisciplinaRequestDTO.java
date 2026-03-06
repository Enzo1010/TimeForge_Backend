package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaRequestDTO {

  @NotBlank(message = "Nome da disciplina e obrigatorio")
  private String nome;

  private String codigo;

  private Boolean requerLaboratorio;
}
