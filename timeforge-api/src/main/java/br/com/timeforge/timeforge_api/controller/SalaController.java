package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.SalaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.SalaResponseDTO;
import br.com.timeforge.timeforge_api.service.SalaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/salas")
@RestController
@Validated
public class SalaController {

  private final SalaService service;

  public SalaController(SalaService service) {
    this.service = service;
  }

  @GetMapping()
  public List<SalaResponseDTO> listarSalas() {
    return service.listarSalas();
  }

  @GetMapping("/{id}")
  public SalaResponseDTO listarSalaId(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    return service.listarSalaId(id);
  }

  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public SalaResponseDTO cadastrarSala(@RequestBody @Valid SalaRequestDTO salaObject) {
    return service.cadastrarSala(salaObject);
  }

  @PutMapping("/{id}")
  public SalaResponseDTO editarSala(
          @PathVariable @Positive(message = "id deve ser maior que zero") Long id,
          @RequestBody @Valid SalaRequestDTO salaObject
  ) {
    return service.editarSala(id, salaObject);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletarSala(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    service.deletarSala(id);
  }
}
