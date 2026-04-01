package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.ProfessorDisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ProfessorDisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.ProfessorDisciplina;
import br.com.timeforge.timeforge_api.exception.DuplicateResourceException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfessorDisciplinaService {

    private final ProfessorDisciplinaRepository repository;
    private final ProfessorRepository professorRepository;
    private final DisciplinaRepository disciplinaRepository;

    public ProfessorDisciplinaService(
            ProfessorDisciplinaRepository repository,
            ProfessorRepository professorRepository,
            DisciplinaRepository disciplinaRepository
    ) {
        this.repository = repository;
        this.professorRepository = professorRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    public List<ProfessorDisciplinaResponseDTO> listarPorProfessor(Long professorId) {
        return repository.findByProfessorId(professorId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProfessorDisciplinaResponseDTO> listarPorDisciplina(Long disciplinaId) {
        return repository.findByDisciplinaId(disciplinaId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ProfessorDisciplinaResponseDTO adicionar(ProfessorDisciplinaRequestDTO dto) {
        if (repository.existsByProfessorIdAndDisciplinaId(dto.getProfessorId(), dto.getDisciplinaId())) {
            throw new DuplicateResourceException(
                    "Professor (id=" + dto.getProfessorId() + ") já está habilitado para a disciplina (id=" + dto.getDisciplinaId() + ")."
            );
        }

        Professor professor = professorRepository.findById(dto.getProfessorId())
                .orElseThrow(() -> new EntityNotFoundException("Professor com id (" + dto.getProfessorId() + ") não encontrado!"));

        Disciplina disciplina = disciplinaRepository.findById(dto.getDisciplinaId())
                .orElseThrow(() -> new EntityNotFoundException("Disciplina com id (" + dto.getDisciplinaId() + ") não encontrada!"));

        ProfessorDisciplina salvo = repository.save(
                ProfessorDisciplina.builder()
                        .professor(professor)
                        .disciplina(disciplina)
                        .build()
        );

        return toResponseDTO(salvo);
    }

    public void remover(Long id) {
        ProfessorDisciplina habilitacao = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Habilitação com id (" + id + ") não encontrada!"));

        repository.delete(habilitacao);
    }

    private ProfessorDisciplinaResponseDTO toResponseDTO(ProfessorDisciplina entity) {
        return new ProfessorDisciplinaResponseDTO(
                entity.getId(),
                entity.getProfessor().getId(),
                entity.getProfessor().getNome(),
                entity.getDisciplina().getId(),
                entity.getDisciplina().getNome(),
                entity.getDisciplina().getCodigo()
        );
    }
}
