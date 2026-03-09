package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurmaDisciplinaRepository extends JpaRepository<TurmaDisciplina, Long> {

    @Override
    @EntityGraph(attributePaths = {"turma", "disciplina", "professor"})
    List<TurmaDisciplina> findAll();

    @Override
    @EntityGraph(attributePaths = {"turma", "disciplina", "professor"})
    Optional<TurmaDisciplina> findById(Long id);

    @EntityGraph(attributePaths = {"turma", "disciplina", "professor"})
    List<TurmaDisciplina> findByTurmaId(Long turmaId);

    boolean existsByTurmaIdAndDisciplinaId(Long turmaId, Long disciplinaId);

    boolean existsByTurmaIdAndDisciplinaIdAndIdNot(Long turmaId, Long disciplinaId, Long id);

    boolean existsByProfessorId(Long professorId);

    boolean existsByTurmaId(Long turmaId);

    boolean existsByDisciplinaId(Long disciplinaId);
}
