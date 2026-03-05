package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Long> {
}