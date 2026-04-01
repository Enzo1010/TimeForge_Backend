package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.ScheduleAulaResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleTurmaResponseDTO;
import br.com.timeforge.timeforge_api.entity.Aula;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.exception.BusinessRuleException;
import br.com.timeforge.timeforge_api.exception.EntityNotFoundException;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulePersistenceService {

    private final AulaRepository aulaRepository;
    private final TurmaRepository turmaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;
    private final SalaRepository salaRepository;
    private final SlotHorarioRepository slotHorarioRepository;

    /**
     * Substitui, em transacao unica, toda a grade da turma pelo resultado gerado.
     * Se qualquer erro ocorrer no meio, o rollback garante consistencia.
     */
    @Transactional
    public void substituirGradeDaTurma(ScheduleGenerationResponseDTO geracao) {
        if (!Boolean.TRUE.equals(geracao.getSucesso())) {
            log.debug("Persistência ignorada porque a geração não teve sucesso: turmaId={}", geracao.getTurmaId());
            return;
        }

        Long turmaId = geracao.getTurmaId();
        if (turmaId == null) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "turmaId ausente na resposta de geração.");
        }

        List<ScheduleAulaResponseDTO> aulasGeradas = Optional.ofNullable(geracao.getAulas()).orElse(List.of());
        log.info("Persistindo grade da turmaId={} com {} aulas", turmaId, aulasGeradas.size());
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new EntityNotFoundException("Turma com id (" + turmaId + ") não encontrada."));

        Set<Long> disciplinaIds = new java.util.HashSet<>();
        Set<Long> professorIds = new java.util.HashSet<>();
        Set<Long> salaIds = new java.util.HashSet<>();
        Set<Long> slotIds = new java.util.HashSet<>();
        for (ScheduleAulaResponseDTO aulaDto : aulasGeradas) {
            validarAulaGerada(aulaDto, turmaId);
            disciplinaIds.add(aulaDto.getDisciplinaId());
            professorIds.add(aulaDto.getProfessorId());
            salaIds.add(aulaDto.getSalaId());
            slotIds.add(aulaDto.getSlotHorarioId());
        }

        Map<Long, Disciplina> disciplinas = toMapById(disciplinaRepository.findAllById(disciplinaIds), Disciplina::getId);
        Map<Long, Professor> professores = toMapById(professorRepository.findAllById(professorIds), Professor::getId);
        Map<Long, Sala> salas = toMapById(salaRepository.findAllById(salaIds), Sala::getId);
        Map<Long, SlotHorario> slots = toMapById(slotHorarioRepository.findAllById(slotIds), SlotHorario::getId);

        validarIntegridadeReferencias(disciplinaIds, disciplinas.keySet(), "disciplinas");
        validarIntegridadeReferencias(professorIds, professores.keySet(), "professores");
        validarIntegridadeReferencias(salaIds, salas.keySet(), "salas");
        validarIntegridadeReferencias(slotIds, slots.keySet(), "slots");

        // Remove grade antiga da turma com delete em lote e sincroniza o contexto
        // para evitar conflito de unicidade na mesma transacao.
        aulaRepository.deleteAllByTurmaId(turmaId);
        aulaRepository.flush();
        log.debug("Grade anterior removida para turmaId={}", turmaId);

        List<Aula> novasAulas = new ArrayList<>();
        for (ScheduleAulaResponseDTO aulaDto : aulasGeradas) {
            novasAulas.add(Aula.builder()
                    .turma(turma)
                    .disciplina(lookup(disciplinas, aulaDto.getDisciplinaId(), "Disciplina"))
                    .professor(lookup(professores, aulaDto.getProfessorId(), "Professor"))
                    .sala(lookup(salas, aulaDto.getSalaId(), "Sala"))
                    .slotHorario(lookup(slots, aulaDto.getSlotHorarioId(), "SlotHorario"))
                    .build());
        }

        aulaRepository.saveAllAndFlush(novasAulas);
        log.info("Persistência concluída para turmaId={} com {} registros em aula", turmaId, novasAulas.size());
    }

    @Transactional(readOnly = true)
    public ScheduleTurmaResponseDTO consultarGradeTurma(Long turmaId) {
        log.debug("Buscando grade persistida da turmaId={}", turmaId);
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new EntityNotFoundException("Turma com id (" + turmaId + ") não encontrada."));

        List<Aula> aulas = aulaRepository.findByTurmaIdOrderBySlotHorario_DiaSemanaAscSlotHorario_HoraInicioAscSlotHorario_HoraFimAsc(turmaId);

        List<ScheduleAulaResponseDTO> aulasResponse = new ArrayList<>();
        int indice = 1;
        for (Aula aula : aulas) {
            aulasResponse.add(ScheduleAulaResponseDTO.builder()
                    .indiceAula(indice++)
                    .turmaDisciplinaId(null)
                    .disciplinaId(aula.getDisciplina().getId())
                    .disciplinaNome(aula.getDisciplina().getNome())
                    .professorId(aula.getProfessor().getId())
                    .professorNome(aula.getProfessor().getNome())
                    .turmaId(aula.getTurma().getId())
                    .turmaNome(aula.getTurma().getNome())
                    .salaId(aula.getSala().getId())
                    .salaNome(aula.getSala().getNome())
                    .slotHorarioId(aula.getSlotHorario().getId())
                    .diaSemana(aula.getSlotHorario().getDiaSemana())
                    .horaInicio(aula.getSlotHorario().getHoraInicio())
                    .horaFim(aula.getSlotHorario().getHoraFim())
                    .build());
        }

        return ScheduleTurmaResponseDTO.builder()
                .turmaId(turma.getId())
                .turmaNome(turma.getNome())
                .totalAulas(aulasResponse.size())
                .aulas(aulasResponse)
                .build();
    }

    private void validarAulaGerada(ScheduleAulaResponseDTO aulaDto, Long turmaIdEsperado) {
        if (aulaDto == null) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Lista de aulas contem item nulo.");
        }

        if (!turmaIdEsperado.equals(aulaDto.getTurmaId())) {
            throw new BusinessRuleException(
                    HttpStatus.BAD_REQUEST,
                    "Aula gerada possui turmaId divergente do esperado: " + aulaDto.getTurmaId()
            );
        }

        if (aulaDto.getDisciplinaId() == null) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Aula gerada sem disciplinaId.");
        }
        if (aulaDto.getProfessorId() == null) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Aula gerada sem professorId.");
        }
        if (aulaDto.getSalaId() == null) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Aula gerada sem salaId.");
        }
        if (aulaDto.getSlotHorarioId() == null) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Aula gerada sem slotHorarioId.");
        }
    }

    private <T> Map<Long, T> toMapById(Iterable<T> entities, Function<T, Long> idExtractor) {
        Map<Long, T> map = new HashMap<>();
        for (T entity : entities) {
            map.put(idExtractor.apply(entity), entity);
        }
        return map;
    }

    private void validarIntegridadeReferencias(Set<Long> esperado, Set<Long> encontrado, String tipo) {
        if (!encontrado.containsAll(esperado)) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, "Referências inválidas detectadas para " + tipo + ".");
        }
    }

    private <T> T lookup(Map<Long, T> map, Long id, String tipo) {
        T value = map.get(id);
        if (value == null) {
            throw new BusinessRuleException(HttpStatus.BAD_REQUEST, tipo + " com id (" + id + ") não encontrado.");
        }
        return value;
    }
}
