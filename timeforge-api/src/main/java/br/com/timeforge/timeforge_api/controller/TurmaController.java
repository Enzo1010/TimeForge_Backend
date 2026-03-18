package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.TurmaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.TurmaResponseDTO;
import br.com.timeforge.timeforge_api.service.TurmaService;
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

@RequestMapping("/turmas")
@RestController
@Validated
public class TurmaController {

  private final TurmaService service;

  public TurmaController(TurmaService service) {
    this.service = service;
  }

  @GetMapping()
  public List<TurmaResponseDTO> listarTurmas() {
    return service.listarTurmas();
  }

  @GetMapping("/{id}")
  public TurmaResponseDTO listarTurmaId(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    return service.listarTurmaId(id);
  }

  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public TurmaResponseDTO cadastrarTurma(@RequestBody @Valid TurmaRequestDTO turmaObject) {
    return service.cadastrarTurma(turmaObject);
  }

  @PutMapping("/{id}")
  public TurmaResponseDTO editarTurma(
          @PathVariable @Positive(message = "id deve ser maior que zero") Long id,
          @RequestBody @Valid TurmaRequestDTO turmaObject
  ) {
    return service.editarTurma(id, turmaObject);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletarTurma(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    service.deletarTurma(id);
  }
}
