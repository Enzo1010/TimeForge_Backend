package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.ProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ProfessorResponseDTO;
import br.com.timeforge.timeforge_api.service.ProfessorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/professores")
@RestController
@Validated
public class ProfessorController {

  private final ProfessorService service;

  public ProfessorController(ProfessorService service) {
    this.service = service;
  }

  @GetMapping()
  public List<ProfessorResponseDTO> listarProfessores() {
    return service.listarProfessores();
  }

  @GetMapping("/{id}")
  public ProfessorResponseDTO listarProfessorId(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    return service.listarProfessorId(id);
  }

  @PostMapping()
  public ProfessorResponseDTO cadatrarProfessor(@RequestBody @Valid ProfessorRequestDTO professorObject) {
    return service.cadastrarProfessor(professorObject);
  }

  @PutMapping("/{id}")
  public ProfessorResponseDTO editarProfessor(@PathVariable @Positive(message = "id deve ser maior que zero") Long id, @RequestBody @Valid ProfessorRequestDTO professorObject){
    return service.editarProfessor(id, professorObject);
  }

  @DeleteMapping("/{id}")
  public void deletarProfessor(@PathVariable @Positive(message = "id deve ser maior que zero")  Long id){
    service.deletarProfessor(id);
  }

}
