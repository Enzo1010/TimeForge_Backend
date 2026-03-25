package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.DisponibilidadeProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.DisponibilidadeProfessorResponseDTO;
import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.exception.DuplicateResourceException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisponibilidadeProfessorService {

    private final DisponibilidadeProfessorRepository disponibilidadeRepository;
    private final ProfessorRepository professorRepository;
    private final SlotHorarioRepository slotHorarioRepository;

    public DisponibilidadeProfessorService(
            DisponibilidadeProfessorRepository disponibilidadeRepository,
            ProfessorRepository professorRepository,
            SlotHorarioRepository slotHorarioRepository
    ) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.professorRepository = professorRepository;
        this.slotHorarioRepository = slotHorarioRepository;
    }

    public List<DisponibilidadeProfessorResponseDTO> listarDisponibilidadesProfessor() {
        return disponibilidadeRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public DisponibilidadeProfessorResponseDTO listarDisponibilidadeProfessorId(Long id) {
        DisponibilidadeProfessor disponibilidade = disponibilidadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "DisponibilidadeProfessor com id (" + id + ") nao encontrada!"
                ));

        return toResponseDTO(disponibilidade);
    }

    public DisponibilidadeProfessorResponseDTO cadastrarDisponibilidadeProfessor(DisponibilidadeProfessorRequestDTO payload) {
        validarDuplicidadeCadastro(payload.getProfessorId(), payload.getSlotHorarioId());

        Professor professor = buscarProfessor(payload.getProfessorId());
        SlotHorario slotHorario = buscarSlotHorario(payload.getSlotHorarioId());

        DisponibilidadeProfessor disponibilidade = DisponibilidadeProfessor.builder()
                .professor(professor)
                .slotHorario(slotHorario)
                .build();

        DisponibilidadeProfessor disponibilidadeSalva = disponibilidadeRepository.save(disponibilidade);
        return toResponseDTO(disponibilidadeSalva);
    }

    public DisponibilidadeProfessorResponseDTO editarDisponibilidadeProfessor(Long id, DisponibilidadeProfessorRequestDTO payload) {
        DisponibilidadeProfessor disponibilidade = disponibilidadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "DisponibilidadeProfessor com id (" + id + ") nao encontrada!"
                ));

        validarDuplicidadeEdicao(payload.getProfessorId(), payload.getSlotHorarioId(), id);

        Professor professor = buscarProfessor(payload.getProfessorId());
        SlotHorario slotHorario = buscarSlotHorario(payload.getSlotHorarioId());

        disponibilidade.setProfessor(professor);
        disponibilidade.setSlotHorario(slotHorario);

        DisponibilidadeProfessor disponibilidadeEditada = disponibilidadeRepository.save(disponibilidade);
        return toResponseDTO(disponibilidadeEditada);
    }

    public void deletarDisponibilidadeProfessor(Long id) {
        DisponibilidadeProfessor disponibilidade = disponibilidadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "DisponibilidadeProfessor com id (" + id + ") nao encontrada!"
                ));

        disponibilidadeRepository.delete(disponibilidade);
    }

    private Professor buscarProfessor(Long professorId) {
        return professorRepository.findById(professorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Professor com id (" + professorId + ") nao encontrado!"
                ));
    }

    private SlotHorario buscarSlotHorario(Long slotHorarioId) {
        return slotHorarioRepository.findById(slotHorarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "SlotHorario com id (" + slotHorarioId + ") nao encontrado!"
                ));
    }

    private void validarDuplicidadeCadastro(Long professorId, Long slotHorarioId) {
        if (disponibilidadeRepository.existsByProfessorIdAndSlotHorarioId(professorId, slotHorarioId)) {
            throw new DuplicateResourceException(
                    "Ja existe disponibilidade para professor (" + professorId + ") no slot (" + slotHorarioId + ")."
            );
        }
    }

    private void validarDuplicidadeEdicao(Long professorId, Long slotHorarioId, Long disponibilidadeId) {
        if (disponibilidadeRepository.existsByProfessorIdAndSlotHorarioIdAndIdNot(professorId, slotHorarioId, disponibilidadeId)) {
            throw new DuplicateResourceException(
                    "Ja existe disponibilidade para professor (" + professorId + ") no slot (" + slotHorarioId + ")."
            );
        }
    }

    private DisponibilidadeProfessorResponseDTO toResponseDTO(DisponibilidadeProfessor entity) {
        return new DisponibilidadeProfessorResponseDTO(
                entity.getId(),
                entity.getProfessor().getId(),
                entity.getProfessor().getNome(),
                entity.getSlotHorario().getId(),
                entity.getSlotHorario().getDiaSemana(),
                entity.getSlotHorario().getHoraInicio(),
                entity.getSlotHorario().getHoraFim()
        );
    }
}
