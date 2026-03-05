package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProfessorService {

  private final ProfessorRepository repository;

  public ProfessorService(ProfessorRepository repository) {
    this.repository = repository;
  }

  public List<Professor> listarProfessores() {
    return repository.findAll();
  }

  public Professor listarProfessorId(Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor com id (" + id + ") não encontrado!"));
  }

  public Professor cadastrarProfessor(Professor professorObject) {
    return repository.save(professorObject);
  }

  public Professor editarProfessor(Long id, Professor professorObject) {
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor com id (" + id + ") não encontrado!"));

    professorEncontrado.setNome(professorObject.getNome());
    return repository.save(professorEncontrado);
  }

  public void deletarProfessor(Long id) {
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor com id (" + id + ") não encontrado!"));

    repository.delete(professorEncontrado);
  }
}