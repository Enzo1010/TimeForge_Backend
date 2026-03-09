package br.com.timeforge.timeforge_api.dto.request;

import br.com.timeforge.timeforge_api.entity.TipoSala;
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
public class SalaRequestDTO {

  @NotBlank(message = "Nome da sala e obrigatorio")
  private String nome;

  @NotNull(message = "Capacidade da sala e obrigatoria")
  @Positive(message = "Capacidade da sala deve ser maior que zero")
  private Integer capacidade;

  @NotNull(message = "Tipo da sala e obrigatorio")
  private TipoSala tipoSala;
}
