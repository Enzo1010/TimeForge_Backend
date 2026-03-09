package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.DisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.DisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisciplinaService {

  private final DisciplinaRepository repository;
  private final TurmaDisciplinaRepository turmaDisciplinaRepository;
  private final AulaRepository aulaRepository;

  public DisciplinaService(
          DisciplinaRepository repository,
          TurmaDisciplinaRepository turmaDisciplinaRepository,
          AulaRepository aulaRepository
  ) {
    this.repository = repository;
    this.turmaDisciplinaRepository = turmaDisciplinaRepository;
    this.aulaRepository = aulaRepository;
  }

  public List<DisciplinaResponseDTO> listarDisciplinas() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public DisciplinaResponseDTO listarDisciplinaId(Long id) {
    Disciplina disciplinaEncontrada = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina com id (" + id + ") nao encontrada!"));

    return toResponseDTO(disciplinaEncontrada);
  }

  public DisciplinaResponseDTO cadastrarDisciplina(DisciplinaRequestDTO disciplinaObject) {
    validarCodigoDuplicadoCadastro(disciplinaObject.getCodigo());

    Disciplina disciplinaSalva = toEntity(disciplinaObject);
    disciplinaSalva = repository.save(disciplinaSalva);

    return toResponseDTO(disciplinaSalva);
  }

  public DisciplinaResponseDTO editarDisciplina(Long id, DisciplinaRequestDTO disciplinaObject) {
    Disciplina disciplinaEncontrada = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina com id (" + id + ") nao encontrada!"));

    validarCodigoDuplicadoEdicao(disciplinaObject.getCodigo(), id);

    disciplinaEncontrada.setNome(disciplinaObject.getNome());
    disciplinaEncontrada.setCodigo(normalizarCodigo(disciplinaObject.getCodigo()));
    disciplinaEncontrada.setRequerLaboratorio(disciplinaObject.getRequerLaboratorio());

    Disciplina disciplinaEditada = repository.save(disciplinaEncontrada);
    return toResponseDTO(disciplinaEditada);
  }

  public void deletarDisciplina(Long id) {
    Disciplina disciplinaEncontrada = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina com id (" + id + ") nao encontrada!"));

    if (turmaDisciplinaRepository.existsByDisciplinaId(id)) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir disciplina com id (" + id + "), pois existem vinculacoes em turma_disciplina."
      );
    }

    if (aulaRepository.existsByDisciplinaId(id)) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir disciplina com id (" + id + "), pois existem aulas vinculadas."
      );
    }

    repository.delete(disciplinaEncontrada);
  }

  private DisciplinaResponseDTO toResponseDTO(Disciplina entity) {
    return new DisciplinaResponseDTO(
            entity.getId(),
            entity.getNome(),
            entity.getCodigo(),
            entity.getRequerLaboratorio()
    );
  }

  private Disciplina toEntity(DisciplinaRequestDTO dto) {
    return Disciplina.builder()
            .nome(dto.getNome())
            .codigo(normalizarCodigo(dto.getCodigo()))
            .requerLaboratorio(dto.getRequerLaboratorio())
            .build();
  }

  private void validarCodigoDuplicadoCadastro(String codigo) {
    String codigoNormalizado = normalizarCodigo(codigo);
    if (!StringUtils.hasText(codigoNormalizado)) {
      return;
    }

    if (repository.existsByCodigo(codigoNormalizado)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe disciplina com codigo (" + codigoNormalizado + ").");
    }
  }

  private void validarCodigoDuplicadoEdicao(String codigo, Long disciplinaId) {
    String codigoNormalizado = normalizarCodigo(codigo);
    if (!StringUtils.hasText(codigoNormalizado)) {
      return;
    }

    if (repository.existsByCodigoAndIdNot(codigoNormalizado, disciplinaId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe disciplina com codigo (" + codigoNormalizado + ").");
    }
  }

  private String normalizarCodigo(String codigo) {
    if (!StringUtils.hasText(codigo)) {
      return null;
    }
    return codigo.trim();
  }
}
