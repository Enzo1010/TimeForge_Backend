package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

}