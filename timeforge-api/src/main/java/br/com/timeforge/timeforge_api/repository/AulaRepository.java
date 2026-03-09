package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.Aula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    @EntityGraph(attributePaths = {"disciplina", "professor", "turma", "sala", "slotHorario"})
    List<Aula> findByTurmaIdOrderBySlotHorario_DiaSemanaAscSlotHorario_HoraInicioAscSlotHorario_HoraFimAsc(Long turmaId);

    @EntityGraph(attributePaths = {"professor", "turma", "sala", "slotHorario"})
    List<Aula> findByTurmaIdNot(Long turmaId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Aula a where a.turma.id = :turmaId")
    int deleteAllByTurmaId(@Param("turmaId") Long turmaId);

    boolean existsByProfessorId(Long professorId);

    boolean existsByTurmaId(Long turmaId);

    boolean existsByDisciplinaId(Long disciplinaId);

    boolean existsBySalaId(Long salaId);

    boolean existsBySlotHorarioId(Long slotHorarioId);
}
