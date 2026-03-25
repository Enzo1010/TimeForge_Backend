package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.SalaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.SalaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaService {

  private final SalaRepository repository;
  private final AulaRepository aulaRepository;

  public SalaService(SalaRepository repository, AulaRepository aulaRepository) {
    this.repository = repository;
    this.aulaRepository = aulaRepository;
  }

  public List<SalaResponseDTO> listarSalas() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public SalaResponseDTO listarSalaId(Long id) {
    Sala salaEncontrada = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sala com id (" + id + ") nao encontrada!"));

    return toResponseDTO(salaEncontrada);
  }

  public SalaResponseDTO cadastrarSala(SalaRequestDTO salaObject) {
    Sala salaSalva = toEntity(salaObject);
    salaSalva = repository.save(salaSalva);
    return toResponseDTO(salaSalva);
  }

  public SalaResponseDTO editarSala(Long id, SalaRequestDTO salaObject) {
    Sala salaEncontrada = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sala com id (" + id + ") nao encontrada!"));

    salaEncontrada.setNome(salaObject.getNome());
    salaEncontrada.setCapacidade(salaObject.getCapacidade());
    salaEncontrada.setTipoSala(salaObject.getTipoSala());

    Sala salaEditada = repository.save(salaEncontrada);
    return toResponseDTO(salaEditada);
  }

  public void deletarSala(Long id) {
    Sala salaEncontrada = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Sala com id (" + id + ") nao encontrada!"));

    if (aulaRepository.existsBySalaId(id)) {
      throw new BusinessRuleException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir sala com id (" + id + "), pois existem aulas vinculadas."
      );
    }

    repository.delete(salaEncontrada);
  }

  private SalaResponseDTO toResponseDTO(Sala entity) {
    return new SalaResponseDTO(
            entity.getId(),
            entity.getNome(),
            entity.getCapacidade(),
            entity.getTipoSala()
    );
  }

  private Sala toEntity(SalaRequestDTO dto) {
    return Sala.builder()
            .nome(dto.getNome())
            .capacidade(dto.getCapacidade())
            .tipoSala(dto.getTipoSala())
            .build();
  }
}
