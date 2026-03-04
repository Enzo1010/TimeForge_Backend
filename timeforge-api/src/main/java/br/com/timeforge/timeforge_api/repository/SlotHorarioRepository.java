package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.domain.SlotHorario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotHorarioRepository extends JpaRepository<SlotHorario, Long> {
}