package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurmaDisciplinaRepository extends JpaRepository<TurmaDisciplina, Long> {

    @EntityGraph(attributePaths = {"turma", "disciplina", "professor"})
    List<TurmaDisciplina> findByTurmaId(Long turmaId);

}
