package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.dto.request.SlotHorarioRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.SlotHorarioResponseDTO;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotHorarioService {

  private final SlotHorarioRepository repository;

  public SlotHorarioService(SlotHorarioRepository repository) {
    this.repository = repository;
  }

  public List<SlotHorarioResponseDTO> listarSlotHorarios() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public SlotHorarioResponseDTO listarSlotHorariosId(Long id) {
    SlotHorario slotHorarioEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot com id (" + id + ") não encontrado!"));

    return toResponseDTO(slotHorarioEncontrado);
  }

  @Transactional
  public SlotHorarioResponseDTO gravarSlotHorario(SlotHorarioRequestDTO slotHorarioObject) {
    validarIntervaloHorario(slotHorarioObject);
    SlotHorario slotHorario = toEntity(slotHorarioObject);

    SlotHorario slotHorarioSalvo = repository.save(slotHorario);

    return toResponseDTO(slotHorarioSalvo);
  }

  @Transactional
  public SlotHorarioResponseDTO editarSlotHorario(Long id, SlotHorarioRequestDTO slotHorarioObject) {
    validarIntervaloHorario(slotHorarioObject);
    SlotHorario slotHorarioEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot com id (" + id + ") não encontrado!"));

    slotHorarioEncontrado.setDiaSemana(slotHorarioObject.getDiaSemana());
    slotHorarioEncontrado.setHoraInicio(slotHorarioObject.getHoraInicio());
    slotHorarioEncontrado.setHoraFim(slotHorarioObject.getHoraFim());

    SlotHorario slotHorarioEditado = repository.save(slotHorarioEncontrado);

    return toResponseDTO(slotHorarioEditado);
  }

  @Transactional
  public void excluirSlotHorario(Long id) {
    if (!repository.existsById(id)) {
      throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "Slot com id (" + id + ") não encontrado!");
    }

    repository.deleteById(id);
  }

  // conversao dto pra entity ---
  private SlotHorarioResponseDTO toResponseDTO(SlotHorario entity) {
    return new SlotHorarioResponseDTO(
            entity.getId(),
            entity.getDiaSemana(),
            entity.getHoraInicio(),
            entity.getHoraFim()
    );
  }

  private SlotHorario toEntity(SlotHorarioRequestDTO dto) {
    return SlotHorario.builder()
            .diaSemana(dto.getDiaSemana())
            .horaInicio(dto.getHoraInicio())
            .horaFim(dto.getHoraFim())
            .build();
  }

  private void validarIntervaloHorario(SlotHorarioRequestDTO dto) {
    if (dto.getHoraInicio() == null || dto.getHoraFim() == null) {
      return;
    }

    if (!dto.getHoraInicio().isBefore(dto.getHoraFim())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "horaInicio deve ser anterior a horaFim.");
    }
  }
}
