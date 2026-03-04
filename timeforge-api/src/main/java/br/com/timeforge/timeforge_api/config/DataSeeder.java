package br.com.timeforge.timeforge_api.config;

import br.com.timeforge.timeforge_api.domain.*;
import br.com.timeforge.timeforge_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;
    private final SalaRepository salaRepository;
    private final SlotHorarioRepository slotHorarioRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Evita duplicar seed toda vez que iniciar
        if (turmaRepository.count() > 0) return;

        // 1) Turma (MVP 1 turma)
        Turma turma9A = turmaRepository.save(Turma.builder()
                .nome("9º A")
                .capacidade(30)
                .build());

        // 2) Salas
        Sala sala101 = salaRepository.save(Sala.builder().nome("Sala 101").capacidade(35).build());
        Sala salaLab = salaRepository.save(Sala.builder().nome("Lab 01").capacidade(30).build());

        // 3) Slots (Seg–Sex, 08-09 e 09-10) -> ajuste como quiser
        List<SlotHorario> slots = new ArrayList<>();
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            slots.add(slotHorarioRepository.save(slot(day, "08:00", "09:00")));
            slots.add(slotHorarioRepository.save(slot(day, "09:00", "10:00")));
        }

        // 4) Professores
        Professor profJoao = professorRepository.save(Professor.builder().nome("João").build());
        Professor profMaria = professorRepository.save(Professor.builder().nome("Maria").build());
        Professor profCarlos = professorRepository.save(Professor.builder().nome("Carlos").build());

        // 5) Disponibilidades (exemplo simples)
        // João: seg/qua/sex 08-10
        addDisponibilidade(profJoao, slots, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        // Maria: ter/qui 08-10
        addDisponibilidade(profMaria, slots, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
        // Carlos: seg–sex 09-10
        addDisponibilidadeApenasSegundoSlot(profCarlos, slots);

        // 6) Disciplinas (catálogo)
        Disciplina matematica = disciplinaRepository.save(Disciplina.builder().nome("Matemática I").codigo("MAT1").requerLaboratorio(false).build());
        Disciplina portugues = disciplinaRepository.save(Disciplina.builder().nome("Português").codigo("POR1").requerLaboratorio(false).build());
        Disciplina informatica = disciplinaRepository.save(Disciplina.builder().nome("Informática").codigo("INF1").requerLaboratorio(true).build());

        // 7) Oferta turma-disciplina (turma + disciplina + professor + carga semanal)
        turmaDisciplinaRepository.save(TurmaDisciplina.builder()
                .turma(turma9A)
                .disciplina(matematica)
                .professor(profJoao)
                .cargaHorariaSemanal(2)
                .build());

        turmaDisciplinaRepository.save(TurmaDisciplina.builder()
                .turma(turma9A)
                .disciplina(portugues)
                .professor(profMaria)
                .cargaHorariaSemanal(2)
                .build());

        turmaDisciplinaRepository.save(TurmaDisciplina.builder()
                .turma(turma9A)
                .disciplina(informatica)
                .professor(profCarlos)
                .cargaHorariaSemanal(1)
                .build());
    }

    private SlotHorario slot(DayOfWeek day, String start, String end) {
        return SlotHorario.builder()
                .diaSemana(day)
                .horaInicio(LocalTime.parse(start))
                .horaFim(LocalTime.parse(end))
                .build();
    }

    private void addDisponibilidade(Professor professor, List<SlotHorario> slots, DayOfWeek... days) {
        Set<DayOfWeek> allowed = new HashSet<>(Arrays.asList(days));
        for (SlotHorario s : slots) {
            if (allowed.contains(s.getDiaSemana())) {
                disponibilidadeProfessorRepository.save(DisponibilidadeProfessor.builder()
                        .professor(professor)
                        .slotHorario(s)
                        .build());
            }
        }
    }

    private void addDisponibilidadeApenasSegundoSlot(Professor professor, List<SlotHorario> slots) {
        for (SlotHorario s : slots) {
            if (s.getHoraInicio().equals(LocalTime.parse("09:00"))) {
                disponibilidadeProfessorRepository.save(DisponibilidadeProfessor.builder()
                        .professor(professor)
                        .slotHorario(s)
                        .build());
            }
        }
    }
}