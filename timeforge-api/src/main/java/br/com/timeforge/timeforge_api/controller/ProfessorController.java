package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.ProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ProfessorResponseDTO;
import br.com.timeforge.timeforge_api.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/professores")
@RestController
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
  public ProfessorResponseDTO listarProfessorId(@PathVariable Long id) {
    return service.listarProfessorId(id);
  }

  @PostMapping()
  public ProfessorResponseDTO cadatrarProfessor(@RequestBody @Valid ProfessorRequestDTO professorObject) {
    return service.cadastrarProfessor(professorObject);
  }

  @PatchMapping("/{id}")
  public ProfessorResponseDTO editarProfessor(@PathVariable Long id, @RequestBody @Valid ProfessorRequestDTO professorObject){
    return service.editarProfessor(id, professorObject);
  }

  @DeleteMapping("/{id}")
  public void deletarProfessor(@PathVariable  Long id){
    service.deletarProfessor(id);
  }

}
