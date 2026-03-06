package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DisciplinaResponseDTO {
  private Long id;
  private String nome;
  private String codigo;
  private Boolean requerLaboratorio;
}
