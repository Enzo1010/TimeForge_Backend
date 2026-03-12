package br.com.timeforge.timeforge_api.engine;

import br.com.timeforge.timeforge_api.dto.request.ScheduleGenerationRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleAulaResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.entity.Aula;
import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.entity.TipoSala;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import br.com.timeforge.timeforge_api.repository.DisponibilidadeProfessorRepository;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import br.com.timeforge.timeforge_api.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduleGenerator {


    private final TurmaRepository turmaRepository;
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;
    private final AulaRepository aulaRepository;
    private final SlotHorarioRepository slotHorarioRepository;
    private final SalaRepository salaRepository;
    private final DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;

    /**
     * Ordenacao cronologica padrao para os slots.
     * Mantem a busca e a resposta previsiveis para depuracao e apresentacao.
     */
    private static final Comparator<SlotHorario> SLOT_ORDER =
            Comparator.comparingInt((SlotHorario slot) -> slot.getDiaSemana() == null ? Integer.MAX_VALUE : slot.getDiaSemana().getValue())
                    .thenComparing(slot -> slot.getHoraInicio() == null ? LocalTime.MAX : slot.getHoraInicio())
                    .thenComparing(slot -> slot.getHoraFim() == null ? LocalTime.MAX : slot.getHoraFim());

    public ScheduleGenerationResponseDTO gerarHorario(ScheduleGenerationRequestDTO payload) {
        if (payload == null || payload.getTurmaId() == null || payload.getTurmaId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "turmaId deve ser maior que zero.");
        }

        Long turmaId = payload.getTurmaId();
        Turma turmaEntity = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma com id (" + turmaId + ") nao encontrada."));

        int capacidadeTurma = capacidadeSegura(turmaEntity.getCapacidade());
        if (capacidadeTurma <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Turma com id (" + turmaId + ") possui capacidade invalida. Ajuste para valor maior que zero."
            );
        }

        TurmaContextDTO turma = new TurmaContextDTO(
                turmaEntity.getId(),
                turmaEntity.getNome(),
                capacidadeTurma
        );

        List<String> observacoes = new ArrayList<>();
        observacoes.add("Geracao executada apenas em memoria. As aulas nao foram persistidas na tabela 'aula'.");

        // 1) Carrega as ofertas da turma (disciplina + professor + carga horaria).
        List<OfertaAulaDTO> ofertas = mapearOfertas(turmaDisciplinaRepository.findByTurmaId(turmaId));
        if (ofertas.isEmpty()) {
            return responseFalha(
                    turma,
                    0,
                    0,
                    "Nao foi possivel gerar grade: a turma nao possui ofertas em TurmaDisciplina.",
                    observacoes
            );
        }

        // 2) Expande TurmaDisciplina em variaveis do CSP.
        // Exemplo: carga 3 de Matematica gera 3 variaveis independentes para alocar.
        List<AulaVariavel> variaveis = expandirVariaveisCsp(ofertas, turma.capacidade(), observacoes);
        if (variaveis.isEmpty()) {
            return responseFalha(
                    turma,
                    0,
                    0,
                    "Nao foi possivel gerar grade: nao existem aulas validas para alocar.",
                    observacoes
            );
        }

        // 3) Dominios de slot: todos os slots cadastrados, em ordem cronologica.
        List<SlotHorario> slotsOrdenados = slotHorarioRepository.findAll()
                .stream()
                .peek(this::validarIntegridadeSlot)
                .sorted(SLOT_ORDER)
                .toList();

        if (slotsOrdenados.isEmpty()) {
            return responseFalha(
                    turma,
                    variaveis.size(),
                    0,
                    "Nao foi possivel gerar grade: nao existem slots de horario cadastrados.",
                    observacoes
            );
        }

        // 4) Dominios de sala com First Fit Decreasing:
        // salas da maior para a menor capacidade.
        List<Sala> salasFfd = salaRepository.findAll()
                .stream()
                .peek(this::validarIntegridadeSala)
                .sorted(Comparator.comparingInt((Sala sala) -> capacidadeSegura(sala.getCapacidade())).reversed())
                .toList();

        if (salasFfd.isEmpty()) {
            return responseFalha(
                    turma,
                    variaveis.size(),
                    0,
                    "Nao foi possivel gerar grade: nao existem salas cadastradas.",
                    observacoes
            );
        }

        // 5) Disponibilidade por professor (professor -> slots permitidos).
        Map<Long, Set<Long>> disponibilidadePorProfessor = carregarDisponibilidadePorProfessor(ofertas);
        registrarAlertasDisponibilidade(ofertas, disponibilidadePorProfessor, observacoes);

        // 5.1) Carrega aulas de outras turmas ja persistidas como restricoes fixas.
        List<Aula> aulasFixas = aulaRepository.findByTurmaIdNot(turmaId);
        if (!aulasFixas.isEmpty()) {
            observacoes.add("Geracao considerou " + aulasFixas.size() + " aulas ja persistidas de outras turmas.");
        }

        // 6) Salas possiveis por oferta, respeitando capacidade e laboratorio.
        Map<Long, List<Sala>> salasPossiveisPorTurmaDisciplina =
                montarSalasPossiveisPorOferta(ofertas, turma.capacidade(), salasFfd, observacoes);

        // Heuristica MRV simplificada:
        // prioriza variaveis com menos opcoes de disponibilidade para reduzir branching.
        variaveis.sort(Comparator
                .comparingInt((AulaVariavel aula) -> disponibilidadePorProfessor.getOrDefault(aula.professorId(), Collections.emptySet()).size())
                .thenComparing(aula -> !aula.requerLaboratorio()));

        // Estruturas de ocupacao para checagem O(1) de conflitos.
        Set<String> ocupacaoProfessorSlot = new HashSet<>();
        Set<String> ocupacaoTurmaSlot = new HashSet<>();
        Set<String> ocupacaoSalaSlot = new HashSet<>();
        aplicarRestricoesFixas(aulasFixas, ocupacaoProfessorSlot, ocupacaoTurmaSlot, ocupacaoSalaSlot);

        List<AlocacaoInterna> alocacoesAtuais = new ArrayList<>();
        BuscaEstado buscaEstado = new BuscaEstado();

        boolean sucesso = backtracking(
                0,
                variaveis,
                slotsOrdenados,
                disponibilidadePorProfessor,
                salasPossiveisPorTurmaDisciplina,
                ocupacaoProfessorSlot,
                ocupacaoTurmaSlot,
                ocupacaoSalaSlot,
                alocacoesAtuais,
                buscaEstado
        );

        // Se nao houve solucao completa, devolvemos a melhor solucao parcial.
        List<AlocacaoInterna> alocacaoFinal = sucesso
                ? new ArrayList<>(alocacoesAtuais)
                : new ArrayList<>(buscaEstado.melhorAlocacaoParcial);

        List<ScheduleAulaResponseDTO> aulasResponse = paraResponseOrdenada(alocacaoFinal);

        if (!sucesso) {
            observacoes.add("Nao foi encontrada solucao completa com as restricoes atuais.");
            return response(
                    false,
                    "Nao foi possivel montar a grade completa sem conflitos.",
                    turma,
                    variaveis.size(),
                    aulasResponse.size(),
                    aulasResponse,
                    observacoes
            );
        }

        observacoes.add("Solucao completa encontrada com CSP + Backtracking.");
        return response(
                true,
                "Grade gerada com sucesso (em memoria).",
                turma,
                variaveis.size(),
                aulasResponse.size(),
                aulasResponse,
                observacoes
        );
    }

    /**
     * Backtracking classico:
     * 1) Escolhe a proxima variavel (aula).
     * 2) Tenta valores possiveis (slot + sala).
     * 3) Valida restricoes.
     * 4) Avanca recursivamente.
     * 5) Em conflito, desfaz (rollback) e tenta proxima opcao.
     */
    private boolean backtracking(
            int indice,
            List<AulaVariavel> variaveis,
            List<SlotHorario> slotsOrdenados,
            Map<Long, Set<Long>> disponibilidadePorProfessor,
            Map<Long, List<Sala>> salasPossiveisPorTurmaDisciplina,
            Set<String> ocupacaoProfessorSlot,
            Set<String> ocupacaoTurmaSlot,
            Set<String> ocupacaoSalaSlot,
            List<AlocacaoInterna> alocacoesAtuais,
            BuscaEstado buscaEstado
    ) {
        if (indice == variaveis.size()) {
            return true;
        }

        // Guarda a melhor profundidade para diagnostico de falha.
        if (alocacoesAtuais.size() > buscaEstado.melhorProfundidade) {
            buscaEstado.melhorProfundidade = alocacoesAtuais.size();
            buscaEstado.melhorAlocacaoParcial = new ArrayList<>(alocacoesAtuais);
        }

        AulaVariavel aulaAtual = variaveis.get(indice);

        Set<Long> slotsDisponiveisProfessor = disponibilidadePorProfessor
                .getOrDefault(aulaAtual.professorId(), Collections.emptySet());
        if (slotsDisponiveisProfessor.isEmpty()) {
            return false;
        }

        List<Sala> salasPossiveis = salasPossiveisPorTurmaDisciplina
                .getOrDefault(aulaAtual.oferta().turmaDisciplinaId(), Collections.emptyList());
        if (salasPossiveis.isEmpty()) {
            return false;
        }

        for (SlotHorario slot : slotsOrdenados) {
            Long slotId = slot.getId();

            // Restricao de disponibilidade do professor.
            if (!slotsDisponiveisProfessor.contains(slotId)) {
                continue;
            }

            String chaveProfessorSlot = chave(aulaAtual.professorId(), slotId);
            String chaveTurmaSlot = chave(aulaAtual.turmaId(), slotId);

            // Restricoes duras:
            // - professor nao pode estar em duas aulas no mesmo slot
            // - turma nao pode estar em duas aulas no mesmo slot
            if (ocupacaoProfessorSlot.contains(chaveProfessorSlot) || ocupacaoTurmaSlot.contains(chaveTurmaSlot)) {
                continue;
            }

            // First Fit Decreasing de salas:
            // salas ja estao em ordem decrescente de capacidade.
            for (Sala sala : salasPossiveis) {
                String chaveSalaSlot = chave(sala.getId(), slotId);

                // Restricao dura de sala.
                if (ocupacaoSalaSlot.contains(chaveSalaSlot)) {
                    continue;
                }

                // Tentativa (commit parcial).
                ocupacaoProfessorSlot.add(chaveProfessorSlot);
                ocupacaoTurmaSlot.add(chaveTurmaSlot);
                ocupacaoSalaSlot.add(chaveSalaSlot);
                alocacoesAtuais.add(new AlocacaoInterna(aulaAtual, slot, sala));

                // Avanca para a proxima variavel.
                if (backtracking(
                        indice + 1,
                        variaveis,
                        slotsOrdenados,
                        disponibilidadePorProfessor,
                        salasPossiveisPorTurmaDisciplina,
                        ocupacaoProfessorSlot,
                        ocupacaoTurmaSlot,
                        ocupacaoSalaSlot,
                        alocacoesAtuais,
                        buscaEstado
                )) {
                    return true;
                }

                // Rollback da tentativa quando gera conflito futuro.
                alocacoesAtuais.remove(alocacoesAtuais.size() - 1);
                ocupacaoProfessorSlot.remove(chaveProfessorSlot);
                ocupacaoTurmaSlot.remove(chaveTurmaSlot);
                ocupacaoSalaSlot.remove(chaveSalaSlot);
            }
        }

        return false;
    }

    private void aplicarRestricoesFixas(
            List<Aula> aulasFixas,
            Set<String> ocupacaoProfessorSlot,
            Set<String> ocupacaoTurmaSlot,
            Set<String> ocupacaoSalaSlot
    ) {
        for (Aula aula : aulasFixas) {
            validarIntegridadeAulaFixa(aula);
            Long slotId = aula.getSlotHorario().getId();
            ocupacaoProfessorSlot.add(chave(aula.getProfessor().getId(), slotId));
            ocupacaoTurmaSlot.add(chave(aula.getTurma().getId(), slotId));
            ocupacaoSalaSlot.add(chave(aula.getSala().getId(), slotId));
        }
    }

    /**
     * Expande cada oferta em aulas individuais.
     * Cada item da lista final vira uma variavel do CSP.
     */
    private List<AulaVariavel> expandirVariaveisCsp(
            List<OfertaAulaDTO> ofertas,
            int capacidadeTurma,
            List<String> observacoes
    ) {
        List<AulaVariavel> variaveis = new ArrayList<>();
        int indiceGlobal = 1;

        for (OfertaAulaDTO oferta : ofertas) {
            int carga = oferta.cargaHorariaSemanal();
            if (carga <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "TurmaDisciplina com id (" + oferta.turmaDisciplinaId() + ") possui cargaHorariaSemanal invalida."
                );
            }

            for (int ordemNaDisciplina = 1; ordemNaDisciplina <= carga; ordemNaDisciplina++) {
                variaveis.add(new AulaVariavel(
                        indiceGlobal++,
                        ordemNaDisciplina,
                        oferta,
                        oferta.professorId(),
                        oferta.turmaId(),
                        capacidadeTurma,
                        oferta.requerLaboratorio()
                ));
            }
        }

        return variaveis;
    }

    private Map<Long, Set<Long>> carregarDisponibilidadePorProfessor(List<OfertaAulaDTO> ofertas) {
        Set<Long> professorIds = ofertas.stream()
                .map(OfertaAulaDTO::professorId)
                .collect(Collectors.toSet());

        List<DisponibilidadeProfessor> disponibilidades = disponibilidadeProfessorRepository.findByProfessorIdIn(professorIds);

        Map<Long, Set<Long>> disponibilidadePorProfessor = new HashMap<>();
        for (DisponibilidadeProfessor disponibilidade : disponibilidades) {
            validarIntegridadeDisponibilidade(disponibilidade);
            disponibilidadePorProfessor
                    .computeIfAbsent(disponibilidade.getProfessor().getId(), key -> new HashSet<>())
                    .add(disponibilidade.getSlotHorario().getId());
        }

        return disponibilidadePorProfessor;
    }

    private void registrarAlertasDisponibilidade(
            List<OfertaAulaDTO> ofertas,
            Map<Long, Set<Long>> disponibilidadePorProfessor,
            List<String> observacoes
    ) {
        Map<Long, String> professorNomePorId = new HashMap<>();
        for (OfertaAulaDTO oferta : ofertas) {
            professorNomePorId.put(oferta.professorId(), oferta.professorNome());
        }

        for (Map.Entry<Long, String> professor : professorNomePorId.entrySet()) {
            Set<Long> slots = disponibilidadePorProfessor.getOrDefault(professor.getKey(), Collections.emptySet());
            if (slots.isEmpty()) {
                observacoes.add("Professor " + professor.getValue() + " (id=" + professor.getKey() + ") nao possui disponibilidade cadastrada.");
            }
        }
    }

    private Map<Long, List<Sala>> montarSalasPossiveisPorOferta(
            List<OfertaAulaDTO> ofertas,
            int capacidadeTurma,
            List<Sala> salasFfd,
            List<String> observacoes
    ) {
        Map<Long, List<Sala>> resultado = new HashMap<>();

        for (OfertaAulaDTO oferta : ofertas) {
            boolean requerLaboratorio = oferta.requerLaboratorio();

            List<Sala> salasPossiveis = salasFfd.stream()
                    .filter(sala -> capacidadeSegura(sala.getCapacidade()) >= capacidadeTurma)
                    .filter(sala -> !requerLaboratorio || salaEhLaboratorio(sala))
                    .toList();

            if (salasPossiveis.isEmpty()) {
                observacoes.add(
                        "Nenhuma sala compativel para TD " + oferta.turmaDisciplinaId()
                                + " (" + oferta.disciplinaNome() + ")."
                                + " Capacidade da turma: " + capacidadeTurma
                                + ", requer laboratorio: " + requerLaboratorio + "."
                );
            }

            resultado.put(oferta.turmaDisciplinaId(), salasPossiveis);
        }

        return resultado;
    }

    /**
     * Regra de laboratorio baseada no tipo explicito da sala.
     */
    private boolean salaEhLaboratorio(Sala sala) {
        return TipoSala.LABORATORIO.equals(sala.getTipoSala());
    }

    private int capacidadeSegura(Integer capacidade) {
        return capacidade == null ? 0 : capacidade;
    }

    private void validarIntegridadeSlot(SlotHorario slot) {
        validarObrigatorio(slot != null, "SlotHorario invalido: registro nulo.");
        validarObrigatorio(slot.getId() != null, "SlotHorario invalido: id ausente.");
        validarObrigatorio(slot.getDiaSemana() != null, "SlotHorario com id (" + slot.getId() + ") sem diaSemana.");
        validarObrigatorio(slot.getHoraInicio() != null, "SlotHorario com id (" + slot.getId() + ") sem horaInicio.");
        validarObrigatorio(slot.getHoraFim() != null, "SlotHorario com id (" + slot.getId() + ") sem horaFim.");
        validarObrigatorio(
                slot.getHoraInicio().isBefore(slot.getHoraFim()),
                "SlotHorario com id (" + slot.getId() + ") possui intervalo invalido: horaInicio deve ser anterior a horaFim."
        );
    }

    private void validarIntegridadeSala(Sala sala) {
        validarObrigatorio(sala != null, "Sala invalida: registro nulo.");
        validarObrigatorio(sala.getId() != null, "Sala invalida: id ausente.");
        validarObrigatorio(capacidadeSegura(sala.getCapacidade()) > 0, "Sala com id (" + sala.getId() + ") possui capacidade invalida.");
        validarObrigatorio(sala.getTipoSala() != null, "Sala com id (" + sala.getId() + ") sem tipoSala.");
    }

    private void validarIntegridadeTurmaDisciplina(TurmaDisciplina oferta) {
        validarObrigatorio(oferta != null, "TurmaDisciplina invalida: registro nulo.");
        validarObrigatorio(oferta.getId() != null, "TurmaDisciplina invalida: id ausente.");
        validarObrigatorio(oferta.getTurma() != null, "TurmaDisciplina com id (" + oferta.getId() + ") sem turma.");
        validarObrigatorio(oferta.getDisciplina() != null, "TurmaDisciplina com id (" + oferta.getId() + ") sem disciplina.");
        validarObrigatorio(oferta.getProfessor() != null, "TurmaDisciplina com id (" + oferta.getId() + ") sem professor.");
        validarObrigatorio(oferta.getTurma().getId() != null, "TurmaDisciplina com id (" + oferta.getId() + ") com turma sem id.");
        validarObrigatorio(oferta.getDisciplina().getId() != null, "TurmaDisciplina com id (" + oferta.getId() + ") com disciplina sem id.");
        validarObrigatorio(oferta.getProfessor().getId() != null, "TurmaDisciplina com id (" + oferta.getId() + ") com professor sem id.");
        validarObrigatorio(
                Optional.ofNullable(oferta.getCargaHorariaSemanal()).orElse(0) > 0,
                "TurmaDisciplina com id (" + oferta.getId() + ") possui cargaHorariaSemanal invalida."
        );
    }

    private void validarIntegridadeDisponibilidade(DisponibilidadeProfessor disponibilidade) {
        validarObrigatorio(disponibilidade != null, "DisponibilidadeProfessor invalida: registro nulo.");
        validarObrigatorio(disponibilidade.getProfessor() != null, "DisponibilidadeProfessor sem professor.");
        validarObrigatorio(disponibilidade.getSlotHorario() != null, "DisponibilidadeProfessor sem slotHorario.");
        validarObrigatorio(disponibilidade.getProfessor().getId() != null, "DisponibilidadeProfessor com professor sem id.");
        validarObrigatorio(disponibilidade.getSlotHorario().getId() != null, "DisponibilidadeProfessor com slotHorario sem id.");
    }

    private void validarIntegridadeAulaFixa(Aula aula) {
        validarObrigatorio(aula != null, "Aula persistida invalida: registro nulo.");
        validarObrigatorio(aula.getProfessor() != null, "Aula persistida invalida: professor ausente.");
        validarObrigatorio(aula.getTurma() != null, "Aula persistida invalida: turma ausente.");
        validarObrigatorio(aula.getSala() != null, "Aula persistida invalida: sala ausente.");
        validarObrigatorio(aula.getSlotHorario() != null, "Aula persistida invalida: slotHorario ausente.");
        validarObrigatorio(aula.getProfessor().getId() != null, "Aula persistida invalida: professor sem id.");
        validarObrigatorio(aula.getTurma().getId() != null, "Aula persistida invalida: turma sem id.");
        validarObrigatorio(aula.getSala().getId() != null, "Aula persistida invalida: sala sem id.");
        validarObrigatorio(aula.getSlotHorario().getId() != null, "Aula persistida invalida: slotHorario sem id.");
    }

    private void validarObrigatorio(boolean condicaoValida, String mensagemErro) {
        if (!condicaoValida) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemErro);
        }
    }

    private List<OfertaAulaDTO> mapearOfertas(List<TurmaDisciplina> ofertas) {
        List<OfertaAulaDTO> ofertasMapeadas = new ArrayList<>();
        for (TurmaDisciplina oferta : ofertas) {
            validarIntegridadeTurmaDisciplina(oferta);
            ofertasMapeadas.add(new OfertaAulaDTO(
                    oferta.getId(),
                    oferta.getDisciplina().getId(),
                    oferta.getDisciplina().getNome(),
                    oferta.getProfessor().getId(),
                    oferta.getProfessor().getNome(),
                    oferta.getTurma().getId(),
                    oferta.getTurma().getNome(),
                    Optional.ofNullable(oferta.getCargaHorariaSemanal()).orElse(0),
                    Boolean.TRUE.equals(oferta.getDisciplina().getRequerLaboratorio())
            ));
        }
        return ofertasMapeadas;
    }

    private String chave(Long esquerda, Long direita) {
        return esquerda + ":" + direita;
    }

    private List<ScheduleAulaResponseDTO> paraResponseOrdenada(List<AlocacaoInterna> alocacoes) {
        return alocacoes.stream()
                .sorted(Comparator
                        .comparing((AlocacaoInterna alocacao) -> alocacao.slot(), SLOT_ORDER)
                        .thenComparing(alocacao -> Optional.ofNullable(alocacao.sala().getNome()).orElse(""))
                        .thenComparingInt(alocacao -> alocacao.variavel().indiceGlobal()))
                .map(this::mapearAulaResponse)
                .toList();
    }

    private ScheduleAulaResponseDTO mapearAulaResponse(AlocacaoInterna alocacao) {
        OfertaAulaDTO oferta = alocacao.variavel().oferta();
        return ScheduleAulaResponseDTO.builder()
                .indiceAula(alocacao.variavel().indiceGlobal())
                .turmaDisciplinaId(oferta.turmaDisciplinaId())
                .disciplinaId(oferta.disciplinaId())
                .disciplinaNome(oferta.disciplinaNome())
                .professorId(oferta.professorId())
                .professorNome(oferta.professorNome())
                .turmaId(oferta.turmaId())
                .turmaNome(oferta.turmaNome())
                .salaId(alocacao.sala().getId())
                .salaNome(alocacao.sala().getNome())
                .slotHorarioId(alocacao.slot().getId())
                .diaSemana(alocacao.slot().getDiaSemana())
                .horaInicio(alocacao.slot().getHoraInicio())
                .horaFim(alocacao.slot().getHoraFim())
                .build();
    }

    private ScheduleGenerationResponseDTO responseFalha(
            TurmaContextDTO turma,
            int totalNecessario,
            int totalAlocado,
            String mensagem,
            List<String> observacoes
    ) {
        return response(false, mensagem, turma, totalNecessario, totalAlocado, List.of(), observacoes);
    }

    private ScheduleGenerationResponseDTO response(
            boolean sucesso,
            String mensagem,
            TurmaContextDTO turma,
            int totalNecessario,
            int totalAlocado,
            List<ScheduleAulaResponseDTO> aulas,
            List<String> observacoes
    ) {
        return ScheduleGenerationResponseDTO.builder()
                .sucesso(sucesso)
                .mensagem(mensagem)
                .turmaId(turma.id())
                .turmaNome(turma.nome())
                .totalAulasNecessarias(totalNecessario)
                .totalAulasAlocadas(totalAlocado)
                .aulas(aulas)
                .observacoes(observacoes)
                .build();
    }

    /**
     * DTO interno com dados minimos da turma para resposta.
     */
    private record TurmaContextDTO(Long id, String nome, int capacidade) {
    }

    /**
     * DTO interno da oferta (snapshot de TurmaDisciplina + relacionamentos).
     * Evita carregar entidades JPA no fluxo principal do algoritmo.
     */
    private record OfertaAulaDTO(
            Long turmaDisciplinaId,
            Long disciplinaId,
            String disciplinaNome,
            Long professorId,
            String professorNome,
            Long turmaId,
            String turmaNome,
            int cargaHorariaSemanal,
            boolean requerLaboratorio
    ) {
    }

    /**
     * Variavel do CSP: uma aula individual derivada de uma oferta.
     */
    private record AulaVariavel(
            int indiceGlobal,
            int ordemNaDisciplina,
            OfertaAulaDTO oferta,
            Long professorId,
            Long turmaId,
            int capacidadeTurma,
            boolean requerLaboratorio
    ) {
    }

    /**
     * Valor atribuido para a variavel: (slot + sala).
     */
    private record AlocacaoInterna(AulaVariavel variavel, SlotHorario slot, Sala sala) {
    }

    /**
     * Estado auxiliar da busca para armazenar melhor parcial.
     */
    private static class BuscaEstado {
        private int melhorProfundidade = 0;
        private List<AlocacaoInterna> melhorAlocacaoParcial = new ArrayList<>();
    }
}
