package br.com.timeforge.timeforge_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorRequestDTO {

  @NotBlank(message = "Nome é obrigatório")
  private String nome;
}