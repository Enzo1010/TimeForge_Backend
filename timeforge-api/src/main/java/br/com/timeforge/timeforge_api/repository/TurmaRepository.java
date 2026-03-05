package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
}