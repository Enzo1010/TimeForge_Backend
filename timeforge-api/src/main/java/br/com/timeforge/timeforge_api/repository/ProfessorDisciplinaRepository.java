package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.ProfessorDisciplina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessorDisciplinaRepository extends JpaRepository<ProfessorDisciplina, Long> {

    @EntityGraph(attributePaths = {"professor", "disciplina"})
    List<ProfessorDisciplina> findAllByOrderByProfessor_NomeAscDisciplina_NomeAsc();

    @EntityGraph(attributePaths = {"professor", "disciplina"})
    List<ProfessorDisciplina> findByProfessorId(Long professorId);

    @EntityGraph(attributePaths = {"professor", "disciplina"})
    List<ProfessorDisciplina> findByDisciplinaId(Long disciplinaId);

    boolean existsByProfessorIdAndDisciplinaId(Long professorId, Long disciplinaId);
}
