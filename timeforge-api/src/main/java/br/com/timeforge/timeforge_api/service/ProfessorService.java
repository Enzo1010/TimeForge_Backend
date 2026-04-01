package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ProfessorResponseDTO;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProfessorService {

  private final ProfessorRepository repository;
  private final TurmaDisciplinaRepository turmaDisciplinaRepository;
  private final AulaRepository aulaRepository;
  private final DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;

  public ProfessorService(
          ProfessorRepository repository,
          TurmaDisciplinaRepository turmaDisciplinaRepository,
          AulaRepository aulaRepository,
          DisponibilidadeProfessorRepository disponibilidadeProfessorRepository
  ) {
    this.repository = repository;
    this.turmaDisciplinaRepository = turmaDisciplinaRepository;
    this.aulaRepository = aulaRepository;
    this.disponibilidadeProfessorRepository = disponibilidadeProfessorRepository;
  }

  public List<ProfessorResponseDTO> listarProfessores() {
    log.debug("Listando professores cadastrados");
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public ProfessorResponseDTO listarProfessorId(Long id) {
    log.debug("Buscando professor por id={}", id);
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Professor com id (" + id + ") nao encontrado!"));

    return toResponseDTO(professorEncontrado);
  }

  public ProfessorResponseDTO cadastrarProfessor(ProfessorRequestDTO professorObject) {
    Professor professorSalvo = toEntity(professorObject);
    professorSalvo = repository.save(professorSalvo);
    log.info("Professor cadastrado com sucesso: id={}, nome={}", professorSalvo.getId(), professorSalvo.getNome());
    return toResponseDTO(professorSalvo);
  }

  public ProfessorResponseDTO editarProfessor(Long id, ProfessorRequestDTO professorObject) {
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Professor com id (" + id + ") nao encontrado!"));

    professorEncontrado.setNome(professorObject.getNome());
    Professor professorEditado = repository.save(professorEncontrado);
    log.info("Professor atualizado com sucesso: id={}, nome={}", professorEditado.getId(), professorEditado.getNome());
    return toResponseDTO(professorEditado);
  }

  public void deletarProfessor(Long id) {
    Professor professorEncontrado = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Professor com id (" + id + ") nao encontrado!"));

    if (turmaDisciplinaRepository.existsByProfessorId(id)) {
      throw new BusinessRuleException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir professor com id (" + id + "), pois existem vinculacoes em turma_disciplina."
      );
    }

    if (disponibilidadeProfessorRepository.existsByProfessorId(id)) {
      throw new BusinessRuleException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir professor com id (" + id + "), pois existem disponibilidades cadastradas."
      );
    }

    if (aulaRepository.existsByProfessorId(id)) {
      throw new BusinessRuleException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir professor com id (" + id + "), pois existem aulas vinculadas."
      );
    }

    repository.delete(professorEncontrado);
    log.info("Professor removido com sucesso: id={}, nome={}", professorEncontrado.getId(), professorEncontrado.getNome());
  }

  private ProfessorResponseDTO toResponseDTO(Professor entity) {
    return new ProfessorResponseDTO(
            entity.getId(),
            entity.getNome()
    );
  }

  private Professor toEntity(ProfessorRequestDTO dto) {
    return Professor.builder()
            .nome(dto.getNome())
            .build();
  }
}
