package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadeProfessorRepository extends JpaRepository<DisponibilidadeProfessor, Long> {

    @Override
    @EntityGraph(attributePaths = {"professor", "slotHorario"})
    List<DisponibilidadeProfessor> findAll();

    @Override
    @EntityGraph(attributePaths = {"professor", "slotHorario"})
    Optional<DisponibilidadeProfessor> findById(Long id);

    @EntityGraph(attributePaths = {"professor", "slotHorario"})
    List<DisponibilidadeProfessor> findByProfessorIdIn(Collection<Long> professorIds);

    boolean existsByProfessorIdAndSlotHorarioId(Long professorId, Long slotHorarioId);

    boolean existsByProfessorIdAndSlotHorarioIdAndIdNot(Long professorId, Long slotHorarioId, Long id);
}
