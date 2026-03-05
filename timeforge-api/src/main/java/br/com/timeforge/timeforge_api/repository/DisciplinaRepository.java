package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
}