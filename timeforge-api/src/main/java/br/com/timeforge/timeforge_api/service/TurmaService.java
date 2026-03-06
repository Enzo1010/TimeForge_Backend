package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.TurmaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.TurmaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurmaService {

  private final TurmaRepository repository;

  public TurmaService(TurmaRepository repository) {
    this.repository = repository;
  }

  public List<TurmaResponseDTO> listarTurmas() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public TurmaResponseDTO listarTurmaId(Long id) {
    Turma turmaEncontrada = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma com id (" + id + ") nao encontrada!"));

    return toResponseDTO(turmaEncontrada);
  }

  public TurmaResponseDTO cadastrarTurma(TurmaRequestDTO turmaObject) {
    Turma turmaSalva = toEntity(turmaObject);
    turmaSalva = repository.save(turmaSalva);

    return toResponseDTO(turmaSalva);
  }

  public TurmaResponseDTO editarTurma(Long id, TurmaRequestDTO turmaObject) {
    Turma turmaEncontrada = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma com id (" + id + ") nao encontrada!"));

    turmaEncontrada.setNome(turmaObject.getNome());
    turmaEncontrada.setCapacidade(turmaObject.getCapacidade());

    Turma turmaEditada = repository.save(turmaEncontrada);
    return toResponseDTO(turmaEditada);
  }

  public void deletarTurma(Long id) {
    Turma turmaEncontrada = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma com id (" + id + ") nao encontrada!"));

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
