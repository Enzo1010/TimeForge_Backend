package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.SlotHorario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;

public interface SlotHorarioRepository extends JpaRepository<SlotHorario, Long> {

    boolean existsByDiaSemanaAndHoraInicioLessThanAndHoraFimGreaterThan(
            DayOfWeek diaSemana,
            LocalTime horaFim,
            LocalTime horaInicio
    );

    boolean existsByDiaSemanaAndHoraInicioLessThanAndHoraFimGreaterThanAndIdNot(
            DayOfWeek diaSemana,
            LocalTime horaFim,
            LocalTime horaInicio,
            Long id
    );
}
