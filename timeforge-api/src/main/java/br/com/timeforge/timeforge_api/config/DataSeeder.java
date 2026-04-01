package br.com.timeforge.timeforge_api.config;

import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.ProfessorDisciplina;
import br.com.timeforge.timeforge_api.entity.Role;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.entity.TipoSala;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
import br.com.timeforge.timeforge_api.entity.Usuario;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import br.com.timeforge.timeforge_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
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
    private final ProfessorDisciplinaRepository professorDisciplinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.admin.nome:Administrador}")
    private String adminNome;

    @Value("${security.admin.email:}")
    private String adminEmail;

    @Value("${security.admin.senha:}")
    private String adminSenha;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();

        if (turmaRepository.count() > 0) {
            log.info("Banco de dados ja populado. Ignorando DataSeeder.");
            return;
        }

        log.info("Iniciando DataSeeder com dados de uma faculdade de tecnologia...");

        // 1) Slots de horario (5 dias x 4 slots = 20 slots)
        List<DayOfWeek> diasUteis = List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        );
        List<SlotHorario> slots = new ArrayList<>();
        for (DayOfWeek day : diasUteis) {
            slots.add(slotHorarioRepository.save(criarSlot(day, "08:00", "09:00")));
            slots.add(slotHorarioRepository.save(criarSlot(day, "09:00", "10:00")));
            slots.add(slotHorarioRepository.save(criarSlot(day, "10:30", "11:30")));
            slots.add(slotHorarioRepository.save(criarSlot(day, "11:30", "12:30")));
        }

        // 2) Salas
        salaRepository.save(Sala.builder().nome("Sala 101 - Bloco A").capacidade(50).tipoSala(TipoSala.COMUM).build());
        salaRepository.save(Sala.builder().nome("Sala 102 - Bloco A").capacidade(40).tipoSala(TipoSala.COMUM).build());
        salaRepository.save(Sala.builder().nome("Sala 103 - Bloco B").capacidade(30).tipoSala(TipoSala.COMUM).build());
        salaRepository.save(Sala.builder().nome("Lab 01 - Redes").capacidade(25).tipoSala(TipoSala.LABORATORIO).build());
        salaRepository.save(Sala.builder().nome("Lab 02 - Software").capacidade(50).tipoSala(TipoSala.LABORATORIO).build());

        // 3) Professores
        Professor profAlanTuring = professorRepository.save(Professor.builder().nome("Alan Turing").build());
        Professor profAdaLovelace = professorRepository.save(Professor.builder().nome("Ada Lovelace").build());
        Professor profLinusTorvalds = professorRepository.save(Professor.builder().nome("Linus Torvalds").build());
        Professor profGraceHopper = professorRepository.save(Professor.builder().nome("Grace Hopper").build());
        Professor profGargalo = professorRepository.save(Professor.builder().nome("Donald Knuth").build());

        // 4) Disponibilidade
        adicionarDisponibilidadeDiasCheios(profAlanTuring, slots, diasUteis);
        adicionarDisponibilidadeDiasCheios(
                profAdaLovelace,
                slots,
                List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        );
        adicionarDisponibilidadeDiasCheios(
                profLinusTorvalds,
                slots,
                List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
        );
        adicionarDisponibilidadeSlotsEspecificos(profGraceHopper, slots, List.of("10:30", "11:30"));
        adicionarDisponibilidadeSlotUnicoDia(profGargalo, slots, DayOfWeek.MONDAY, List.of("08:00", "09:00"));

        // 5) Disciplinas
        Disciplina calc1 = disciplinaRepository.save(
                Disciplina.builder().nome("Calculo I").codigo("MAT01").requerLaboratorio(false).build()
        );
        Disciplina alg1 = disciplinaRepository.save(
                Disciplina.builder().nome("Algoritmos e Logica").codigo("COMP01").requerLaboratorio(true).build()
        );
        Disciplina so = disciplinaRepository.save(
                Disciplina.builder().nome("Sistemas Operacionais").codigo("COMP02").requerLaboratorio(true).build()
        );
        Disciplina engSoft = disciplinaRepository.save(
                Disciplina.builder().nome("Engenharia de Software").codigo("COMP05").requerLaboratorio(false).build()
        );
        Disciplina teoriaDaComputacaoAvancada = disciplinaRepository.save(
                Disciplina.builder().nome("Teoria da Computacao Avancada").codigo("COMP99").requerLaboratorio(false).build()
        );

        // 6) Turmas
        Turma cc1Semestre = turmaRepository.save(
                Turma.builder().nome("Ciencia da Computacao - 1 Semestre").capacidade(45).build()
        );
        Turma cc3Semestre = turmaRepository.save(
                Turma.builder().nome("Ciencia da Computacao - 3 Semestre").capacidade(35).build()
        );
        Turma ccGargalo = turmaRepository.save(
                Turma.builder().nome("Ciencia da Computacao - Turma Gargalo CSP").capacidade(30).build()
        );

        // 7) Habilitações (quais disciplinas cada professor pode lecionar)
        vincularHabilitacao(profAlanTuring, calc1);
        vincularHabilitacao(profAdaLovelace, alg1);
        vincularHabilitacao(profLinusTorvalds, so);
        vincularHabilitacao(profGraceHopper, engSoft);
        vincularHabilitacao(profGargalo, teoriaDaComputacaoAvancada);

        // 8) Ofertas (Turma x Disciplina x Professor)
        vincularOferta(cc1Semestre, alg1, profAdaLovelace, 4);
        vincularOferta(cc1Semestre, calc1, profAlanTuring, 4);

        vincularOferta(cc3Semestre, so, profLinusTorvalds, 4);
        vincularOferta(cc3Semestre, engSoft, profGraceHopper, 4);

        vincularOferta(ccGargalo, teoriaDaComputacaoAvancada, profGargalo, 8);

        log.info(
                "Cenario de gargalo criado: turma '{}' (id={}) exige 8 aulas com professor '{}' em apenas 2 slots.",
                ccGargalo.getNome(),
                ccGargalo.getId(),
                profGargalo.getNome()
        );
        log.info("Para reproduzir a grade parcial: POST /schedule/generate/{}", ccGargalo.getId());

        log.info("DataSeeder concluido. Massa de dados inserida com sucesso.");
    }

    private void seedAdmin() {
        if (adminEmail == null || adminEmail.isBlank() || adminSenha == null || adminSenha.isBlank()) {
            log.info("Admin: email ou senha nao configurados. Ignorando seed de admin.");
            return;
        }

        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.info("Admin: usuario admin ja cadastrado.");
            return;
        }

        Usuario admin = Usuario.builder()
                .nome(adminNome == null || adminNome.isBlank() ? "Administrador" : adminNome)
                .email(adminEmail)
                .senhaHash(passwordEncoder.encode(adminSenha))
                .role(Role.ADMIN)
                .build();

        usuarioRepository.save(admin);
        log.info("Admin: usuario admin criado com sucesso.");
    }

    private SlotHorario criarSlot(DayOfWeek day, String start, String end) {
        return SlotHorario.builder()
                .diaSemana(day)
                .horaInicio(LocalTime.parse(start))
                .horaFim(LocalTime.parse(end))
                .build();
    }

    private void adicionarDisponibilidadeDiasCheios(
            Professor professor,
            List<SlotHorario> todosSlots,
            List<DayOfWeek> diasPermitidos
    ) {
        Set<DayOfWeek> permitidosSet = new HashSet<>(diasPermitidos);
        for (SlotHorario slot : todosSlots) {
            if (permitidosSet.contains(slot.getDiaSemana())) {
                salvarDisponibilidade(professor, slot);
            }
        }
    }

    private void adicionarDisponibilidadeSlotsEspecificos(
            Professor professor,
            List<SlotHorario> todosSlots,
            List<String> horariosInicioPermitidos
    ) {
        for (SlotHorario slot : todosSlots) {
            if (horariosInicioPermitidos.contains(slot.getHoraInicio().toString())) {
                salvarDisponibilidade(professor, slot);
            }
        }
    }

    private void adicionarDisponibilidadeSlotUnicoDia(
            Professor professor,
            List<SlotHorario> todosSlots,
            DayOfWeek diaPermitido,
            List<String> horariosInicioPermitidos
    ) {
        for (SlotHorario slot : todosSlots) {
            if (diaPermitido.equals(slot.getDiaSemana()) && horariosInicioPermitidos.contains(slot.getHoraInicio().toString())) {
                salvarDisponibilidade(professor, slot);
            }
        }
    }

    private void salvarDisponibilidade(Professor prof, SlotHorario slot) {
        disponibilidadeProfessorRepository.save(DisponibilidadeProfessor.builder()
                .professor(prof)
                .slotHorario(slot)
                .build());
    }

    private void vincularHabilitacao(Professor professor, Disciplina disciplina) {
        professorDisciplinaRepository.save(ProfessorDisciplina.builder()
                .professor(professor)
                .disciplina(disciplina)
                .build());
    }

    private void vincularOferta(Turma turma, Disciplina disciplina, Professor professor, int carga) {
        turmaDisciplinaRepository.save(TurmaDisciplina.builder()
                .turma(turma)
                .disciplina(disciplina)
                .professor(professor)
                .cargaHorariaSemanal(carga)
                .build());
    }
}
