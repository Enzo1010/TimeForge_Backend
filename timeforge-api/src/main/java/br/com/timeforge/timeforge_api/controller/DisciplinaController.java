package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.DisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.DisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.service.DisciplinaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/disciplinas")
@RestController
@Validated
public class DisciplinaController {

  private final DisciplinaService service;

  public DisciplinaController(DisciplinaService service) {
    this.service = service;
  }

  @GetMapping()
  public List<DisciplinaResponseDTO> listarDisciplinas() {
    return service.listarDisciplinas();
  }

  @GetMapping("/{id}")
  public DisciplinaResponseDTO listarDisciplinaId(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    return service.listarDisciplinaId(id);
  }

  @PostMapping()
  public DisciplinaResponseDTO cadastrarDisciplina(@RequestBody @Valid DisciplinaRequestDTO disciplinaObject) {
    return service.cadastrarDisciplina(disciplinaObject);
  }

  @PutMapping("/{id}")
  public DisciplinaResponseDTO editarDisciplina(
          @PathVariable @Positive(message = "id deve ser maior que zero") Long id,
          @RequestBody @Valid DisciplinaRequestDTO disciplinaObject
  ) {
    return service.editarDisciplina(id, disciplinaObject);
  }

  @DeleteMapping("/{id}")
  public void deletarDisciplina(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    service.deletarDisciplina(id);
  }
}
