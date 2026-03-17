package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.SlotHorarioRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.SlotHorarioResponseDTO;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotHorarioService {

  private final SlotHorarioRepository repository;
  private final AulaRepository aulaRepository;
  private final DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;

  public SlotHorarioService(
          SlotHorarioRepository repository,
          AulaRepository aulaRepository,
          DisponibilidadeProfessorRepository disponibilidadeProfessorRepository
  ) {
    this.repository = repository;
    this.aulaRepository = aulaRepository;
    this.disponibilidadeProfessorRepository = disponibilidadeProfessorRepository;
  }

  public List<SlotHorarioResponseDTO> listarSlotHorarios() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public SlotHorarioResponseDTO listarSlotHorariosId(Long id) {
    SlotHorario slotHorarioEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot com id (" + id + ") nao encontrado!"));

    return toResponseDTO(slotHorarioEncontrado);
  }

  @Transactional
  public SlotHorarioResponseDTO gravarSlotHorario(SlotHorarioRequestDTO slotHorarioObject) {
    validarIntervaloHorario(slotHorarioObject);
    validarSobreposicaoCadastro(slotHorarioObject);
    SlotHorario slotHorario = toEntity(slotHorarioObject);

    SlotHorario slotHorarioSalvo = repository.save(slotHorario);
    return toResponseDTO(slotHorarioSalvo);
  }

  @Transactional
  public SlotHorarioResponseDTO editarSlotHorario(Long id, SlotHorarioRequestDTO slotHorarioObject) {
    validarIntervaloHorario(slotHorarioObject);
    validarSobreposicaoEdicao(id, slotHorarioObject);
    SlotHorario slotHorarioEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot com id (" + id + ") nao encontrado!"));

    slotHorarioEncontrado.setDiaSemana(slotHorarioObject.getDiaSemana());
    slotHorarioEncontrado.setHoraInicio(slotHorarioObject.getHoraInicio());
    slotHorarioEncontrado.setHoraFim(slotHorarioObject.getHoraFim());

    SlotHorario slotHorarioEditado = repository.save(slotHorarioEncontrado);
    return toResponseDTO(slotHorarioEditado);
  }

  @Transactional
  public void excluirSlotHorario(Long id) {
    SlotHorario slotHorarioEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot com id (" + id + ") nao encontrado!"));

    if (disponibilidadeProfessorRepository.existsBySlotHorarioId(id)) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir slot com id (" + id + "), pois existem disponibilidades de professor vinculadas."
      );
    }

    if (aulaRepository.existsBySlotHorarioId(id)) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Nao e possivel excluir slot com id (" + id + "), pois existem aulas vinculadas."
      );
    }

    repository.delete(slotHorarioEncontrado);
  }

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

  private void validarSobreposicaoCadastro(SlotHorarioRequestDTO dto) {
    if (dto.getDiaSemana() == null || dto.getHoraInicio() == null || dto.getHoraFim() == null) {
      return;
    }

    if (repository.existsByDiaSemanaAndHoraInicioLessThanAndHoraFimGreaterThan(
            dto.getDiaSemana(),
            dto.getHoraFim(),
            dto.getHoraInicio()
    )) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Ja existe slot sobreposto para o dia informado."
      );
    }
  }

  private void validarSobreposicaoEdicao(Long id, SlotHorarioRequestDTO dto) {
    if (dto.getDiaSemana() == null || dto.getHoraInicio() == null || dto.getHoraFim() == null) {
      return;
    }

    if (repository.existsByDiaSemanaAndHoraInicioLessThanAndHoraFimGreaterThanAndIdNot(
            dto.getDiaSemana(),
            dto.getHoraFim(),
            dto.getHoraInicio(),
            id
    )) {
      throw new ResponseStatusException(
              HttpStatus.CONFLICT,
              "Ja existe slot sobreposto para o dia informado."
      );
    }
  }
}
