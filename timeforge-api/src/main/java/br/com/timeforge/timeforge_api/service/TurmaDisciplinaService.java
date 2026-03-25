package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.request.TurmaDisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.TurmaDisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
import br.com.timeforge.timeforge_api.exception.DuplicateResourceException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurmaDisciplinaService {

    private final TurmaDisciplinaRepository repository;
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;

    public TurmaDisciplinaService(
            TurmaDisciplinaRepository repository,
            TurmaRepository turmaRepository,
            DisciplinaRepository disciplinaRepository,
            ProfessorRepository professorRepository
    ) {
        this.repository = repository;
        this.turmaRepository = turmaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.professorRepository = professorRepository;
    }

    public List<TurmaDisciplinaResponseDTO> listarTurmasDisciplinas() {
        return repository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TurmaDisciplinaResponseDTO listarTurmaDisciplinaId(Long id) {
        TurmaDisciplina turmaDisciplina = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TurmaDisciplina com id (" + id + ") nao encontrada!"
                ));

        return toResponseDTO(turmaDisciplina);
    }

    public TurmaDisciplinaResponseDTO cadastrarTurmaDisciplina(TurmaDisciplinaRequestDTO payload) {
        validarDuplicidadeCadastro(payload.getTurmaId(), payload.getDisciplinaId());

        TurmaDisciplina turmaDisciplina = TurmaDisciplina.builder()
                .turma(buscarTurma(payload.getTurmaId()))
                .disciplina(buscarDisciplina(payload.getDisciplinaId()))
                .professor(buscarProfessor(payload.getProfessorId()))
                .cargaHorariaSemanal(payload.getCargaHorariaSemanal())
                .build();

        TurmaDisciplina turmaDisciplinaSalva = repository.save(turmaDisciplina);
        return toResponseDTO(turmaDisciplinaSalva);
    }

    public TurmaDisciplinaResponseDTO editarTurmaDisciplina(Long id, TurmaDisciplinaRequestDTO payload) {
        TurmaDisciplina turmaDisciplina = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TurmaDisciplina com id (" + id + ") nao encontrada!"
                ));

        validarDuplicidadeEdicao(payload.getTurmaId(), payload.getDisciplinaId(), id);

        turmaDisciplina.setTurma(buscarTurma(payload.getTurmaId()));
        turmaDisciplina.setDisciplina(buscarDisciplina(payload.getDisciplinaId()));
        turmaDisciplina.setProfessor(buscarProfessor(payload.getProfessorId()));
        turmaDisciplina.setCargaHorariaSemanal(payload.getCargaHorariaSemanal());

        TurmaDisciplina turmaDisciplinaEditada = repository.save(turmaDisciplina);
        return toResponseDTO(turmaDisciplinaEditada);
    }

    public void deletarTurmaDisciplina(Long id) {
        TurmaDisciplina turmaDisciplina = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TurmaDisciplina com id (" + id + ") nao encontrada!"
                ));

        repository.delete(turmaDisciplina);
    }

    private Turma buscarTurma(Long turmaId) {
        return turmaRepository.findById(turmaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Turma com id (" + turmaId + ") nao encontrada!"
                ));
    }

    private Disciplina buscarDisciplina(Long disciplinaId) {
        return disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Disciplina com id (" + disciplinaId + ") nao encontrada!"
                ));
    }

    private Professor buscarProfessor(Long professorId) {
        return professorRepository.findById(professorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Professor com id (" + professorId + ") nao encontrado!"
                ));
    }

    private void validarDuplicidadeCadastro(Long turmaId, Long disciplinaId) {
        if (repository.existsByTurmaIdAndDisciplinaId(turmaId, disciplinaId)) {
            throw new DuplicateResourceException(
                    "Ja existe vinculacao da turma (" + turmaId + ") com disciplina (" + disciplinaId + ")."
            );
        }
    }

    private void validarDuplicidadeEdicao(Long turmaId, Long disciplinaId, Long id) {
        if (repository.existsByTurmaIdAndDisciplinaIdAndIdNot(turmaId, disciplinaId, id)) {
            throw new DuplicateResourceException(
                    "Ja existe vinculacao da turma (" + turmaId + ") com disciplina (" + disciplinaId + ")."
            );
        }
    }

    private TurmaDisciplinaResponseDTO toResponseDTO(TurmaDisciplina entity) {
        return new TurmaDisciplinaResponseDTO(
                entity.getId(),
                entity.getTurma().getId(),
                entity.getTurma().getNome(),
                entity.getDisciplina().getId(),
                entity.getDisciplina().getNome(),
                entity.getProfessor().getId(),
                entity.getProfessor().getNome(),
                entity.getCargaHorariaSemanal(),
                entity.getDisciplina().getRequerLaboratorio()
        );
    }
}
