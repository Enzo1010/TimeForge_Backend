package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.domain.TurmaDisciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurmaDisciplinaRepository extends JpaRepository<TurmaDisciplina, Long> {

    List<TurmaDisciplina> findByTurmaId(Long turmaId);

}