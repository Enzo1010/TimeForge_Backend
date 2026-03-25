package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.TurmaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.TurmaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurmaService {

  private final TurmaRepository repository;
  private final TurmaDisciplinaRepository turmaDisciplinaRepository;
  private final AulaRepository aulaRepository;

  public TurmaService(
          TurmaRepository repository,
          TurmaDisciplinaRepository turmaDisciplinaRepository,
          AulaRepository aulaRepository
  ) {
    this.repository = repository;
    this.turmaDisciplinaRepository = turmaDisciplinaRepository;
    this.aulaRepository = aulaRepository;
  }

  public List<TurmaResponseDTO> listarTurmas() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public TurmaResponseDTO listarTurmaId(Long id) {
    Turma turmaEncontrada = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Turma com id (" + id + ") nao encontrada!"));

    return toResponseDTO(turmaEncontrada);
  }

  public TurmaResponseDTO cadastrarTurma(TurmaRequestDTO turmaObject) {
    Turma turmaSalva = toEntity(turmaObject);
    turmaSalva = repository.save(turmaSalva);
    return toResponseDTO(turmaSalva);
  }

  public TurmaResponseDTO editarTurma(Long id, TurmaRequestDTO turmaObject) {
    Turma turmaEncontrada = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Turma com id (" + id + ") nao encontrada!"));

    turmaEncontrada.setNome(turmaObject.getNome());
    turmaEncontrada.setCapacidade(turmaObject.getCapacidade());

    Turma turmaEditada = repository.save(turmaEncontrada);
    return toResponseDTO(turmaEditada);
  }

  public void deletarTurma(Long id) {
    Turma turmaEncontrada = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Turma com id (" + id + ") nao encontrada!"));

    if (turmaDisciplinaRepository.existsByTurmaId(id)) {
      throw new BusinessRuleException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir turma com id (" + id + "), pois existem vinculacoes em turma_disciplina."
      );
    }

    if (aulaRepository.existsByTurmaId(id)) {
      throw new BusinessRuleException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir turma com id (" + id + "), pois existem aulas vinculadas."
      );
    }

    repository.delete(turmaEncontrada);
  }

  private TurmaResponseDTO toResponseDTO(Turma entity) {
    return new TurmaResponseDTO(
            entity.getId(),
            entity.getNome(),
            entity.getCapacidade()
    );
  }

  private Turma toEntity(TurmaRequestDTO dto) {
    return Turma.builder()
            .nome(dto.getNome())
            .capacidade(dto.getCapacidade())
            .build();
  }
}
