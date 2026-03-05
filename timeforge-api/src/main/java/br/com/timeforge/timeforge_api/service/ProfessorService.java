package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ProfessorResponseDTO;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfessorService {

  private final ProfessorRepository repository;

  public ProfessorService(ProfessorRepository repository) {
    this.repository = repository;
  }

  public List<ProfessorResponseDTO> listarProfessores() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public ProfessorResponseDTO listarProfessorId(Long id) {
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor com id (" + id + ") não encontrado!"));

    return toResponseDTO(professorEncontrado);
  }

  public ProfessorResponseDTO cadastrarProfessor(ProfessorRequestDTO professorObject) {
    Professor professorSalvo = toEntity(professorObject);

    professorSalvo = repository.save(professorSalvo);

    return toResponseDTO(professorSalvo);
  }

  public ProfessorResponseDTO editarProfessor(Long id, ProfessorRequestDTO professorObject) {
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor com id (" + id + ") não encontrado!"));

    professorEncontrado.setNome(professorObject.getNome());

    Professor professorEditado = repository.save(professorEncontrado);

    return toResponseDTO(professorEditado);
  }

  public void deletarProfessor(Long id) {
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor com id (" + id + ") não encontrado!"));

    repository.delete(professorEncontrado);
  }

  // conversao dto para entity
  private ProfessorResponseDTO toResponseDTO(Professor entity){
    return new ProfessorResponseDTO(
            entity.getId(),
            entity.getNome()
    );
  }

  private Professor toEntity(ProfessorRequestDTO dto){
    return Professor.builder()
            .nome((dto.getNome()))
            .build();
  }

}