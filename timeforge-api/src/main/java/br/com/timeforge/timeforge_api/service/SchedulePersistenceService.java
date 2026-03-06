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
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
            return;
        }

        Long turmaId = geracao.getTurmaId();
        if (turmaId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "turmaId ausente na resposta de geracao.");
        }

        List<ScheduleAulaResponseDTO> aulasGeradas = Optional.ofNullable(geracao.getAulas()).orElse(List.of());
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma com id (" + turmaId + ") nao encontrada."));

        Set<Long> disciplinaIds = aulasGeradas.stream().map(ScheduleAulaResponseDTO::getDisciplinaId).collect(java.util.stream.Collectors.toSet());
        Set<Long> professorIds = aulasGeradas.stream().map(ScheduleAulaResponseDTO::getProfessorId).collect(java.util.stream.Collectors.toSet());
        Set<Long> salaIds = aulasGeradas.stream().map(ScheduleAulaResponseDTO::getSalaId).collect(java.util.stream.Collectors.toSet());
        Set<Long> slotIds = aulasGeradas.stream().map(ScheduleAulaResponseDTO::getSlotHorarioId).collect(java.util.stream.Collectors.toSet());

        Map<Long, Disciplina> disciplinas = toMapById(disciplinaRepository.findAllById(disciplinaIds), Disciplina::getId);
        Map<Long, Professor> professores = toMapById(professorRepository.findAllById(professorIds), Professor::getId);
        Map<Long, Sala> salas = toMapById(salaRepository.findAllById(salaIds), Sala::getId);
        Map<Long, SlotHorario> slots = toMapById(slotHorarioRepository.findAllById(slotIds), SlotHorario::getId);

        validarIntegridadeReferencias(disciplinaIds, disciplinas.keySet(), "disciplinas");
        validarIntegridadeReferencias(professorIds, professores.keySet(), "professores");
        validarIntegridadeReferencias(salaIds, salas.keySet(), "salas");
        validarIntegridadeReferencias(slotIds, slots.keySet(), "slots");

        // Remove grade antiga da turma antes de inserir a nova.
        aulaRepository.deleteByTurmaId(turmaId);

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

        aulaRepository.saveAll(novasAulas);
    }

    @Transactional(readOnly = true)
    public ScheduleTurmaResponseDTO consultarGradeTurma(Long turmaId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma com id (" + turmaId + ") nao encontrada."));

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

    private <T> Map<Long, T> toMapById(Iterable<T> entities, Function<T, Long> idExtractor) {
        Map<Long, T> map = new HashMap<>();
        for (T entity : entities) {
            map.put(idExtractor.apply(entity), entity);
        }
        return map;
    }

    private void validarIntegridadeReferencias(Set<Long> esperado, Set<Long> encontrado, String tipo) {
        if (!encontrado.containsAll(esperado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Referencias invalidas detectadas para " + tipo + ".");
        }
    }

    private <T> T lookup(Map<Long, T> map, Long id, String tipo) {
        T value = map.get(id);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, tipo + " com id (" + id + ") nao encontrado.");
        }
        return value;
    }
}
