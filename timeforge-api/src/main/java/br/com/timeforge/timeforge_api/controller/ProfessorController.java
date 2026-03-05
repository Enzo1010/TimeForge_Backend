package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.service.ProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/professor")
@RestController
public class ProfessorController {

  private final ProfessorService service;

  public ProfessorController(ProfessorService service) {
    this.service = service;
  }

  @GetMapping("/listar")
  public List<Professor> listarProfessores() {
    return service.listarProfessores();
  }

  @GetMapping("/listar/{id}")
  public Professor listarProfessorId(@PathVariable Long id) {
    return service.listarProfessorId(id);
  }

  @PostMapping("/cadastrar")
  public Professor cadatrarProfessor(@RequestBody Professor professorObject) {
    service.cadastrarProfessor(professorObject);
    return professorObject;
  }

  @PatchMapping("/editar/{id}")
  public Professor editarProfessor(@PathVariable Long id, @RequestBody Professor professorObject){
    service.editarProfessor(id, professorObject);
    return professorObject;
  }

  @DeleteMapping("/deletar/{id}")
  public ResponseEntity<String> deletarProfessor(@PathVariable  Long id){
    service.deletarProfessor(id);
    return ResponseEntity.ok("Professor excluido com sucesso!");

  }

}