package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.Aula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, Long> {

    @EntityGraph(attributePaths = {"disciplina", "professor", "turma", "sala", "slotHorario"})
    List<Aula> findByTurmaIdOrderBySlotHorario_DiaSemanaAscSlotHorario_HoraInicioAscSlotHorario_HoraFimAsc(Long turmaId);

    @EntityGraph(attributePaths = {"professor", "turma", "sala", "slotHorario"})
    List<Aula> findByTurmaIdNot(Long turmaId);

    void deleteByTurmaId(Long turmaId);
}
