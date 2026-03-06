package br.com.timeforge.timeforge_api.dto.response;

import br.com.timeforge.timeforge_api.entity.TipoSala;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaResponseDTO {
  private Long id;
  private String nome;
  private Integer capacidade;
  private TipoSala tipoSala;
}
