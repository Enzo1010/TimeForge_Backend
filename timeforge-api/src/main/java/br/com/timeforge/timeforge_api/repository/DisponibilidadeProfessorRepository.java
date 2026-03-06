package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DisponibilidadeProfessorRepository extends JpaRepository<DisponibilidadeProfessor, Long> {

    @EntityGraph(attributePaths = {"professor", "slotHorario"})
    List<DisponibilidadeProfessor> findByProfessorIdIn(Collection<Long> professorIds);
}
