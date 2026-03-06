package br.com.timeforge.timeforge_api.engine;

import br.com.timeforge.timeforge_api.dto.response.ScheduleAulaResponseDTO;
import br.com.timeforge.timeforge_api.dto.response.ScheduleGenerationResponseDTO;
import br.com.timeforge.timeforge_api.entity.DisponibilidadeProfessor;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.entity.TipoSala;
import br.com.timeforge.timeforge_api.entity.Turma;
import br.com.timeforge.timeforge_api.entity.TurmaDisciplina;
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

    /**
     * Implementacao do gerador em memoria usando:
     * - CSP (cada aula individual e uma variavel)
     * - Backtracking (busca por tentativa e erro com rollback)
     * - First Fit Decreasing para priorizar a escolha de salas.
     *
     * Nesta etapa nao ha persistencia da grade final em tabela "aula".
     * O objetivo aqui e montar e retornar uma grade valida em memoria.
     */
    private final TurmaRepository turmaRepository;
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;
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

    public ScheduleGenerationResponseDTO gerarHorario(Long turmaId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma com id (" + turmaId + ") nao encontrada."));

        List<String> observacoes = new ArrayList<>();
        observacoes.add("Geracao executada apenas em memoria. As aulas nao foram persistidas na tabela 'aula'.");

        // 1) Carrega as ofertas da turma (disciplina + professor + carga horaria).
        List<TurmaDisciplina> ofertas = turmaDisciplinaRepository.findByTurmaId(turmaId);
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
        List<AulaVariavel> variaveis = expandirVariaveisCsp(ofertas, capacidadeSegura(turma.getCapacidade()), observacoes);
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

        // 6) Salas possiveis por oferta, respeitando capacidade e laboratorio.
        Map<Long, List<Sala>> salasPossiveisPorTurmaDisciplina =
                montarSalasPossiveisPorOferta(ofertas, capacidadeSegura(turma.getCapacidade()), salasFfd, observacoes);

        // Heuristica MRV simplificada:
        // prioriza variaveis com menos opcoes de disponibilidade para reduzir branching.
        variaveis.sort(Comparator
                .comparingInt((AulaVariavel aula) -> disponibilidadePorProfessor.getOrDefault(aula.professorId(), Collections.emptySet()).size())
                .thenComparing(aula -> !aula.requerLaboratorio()));

        // Estruturas de ocupacao para checagem O(1) de conflitos.
        Set<String> ocupacaoProfessorSlot = new HashSet<>();
        Set<String> ocupacaoTurmaSlot = new HashSet<>();
        Set<String> ocupacaoSalaSlot = new HashSet<>();

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
                .getOrDefault(aulaAtual.turmaDisciplina().getId(), Collections.emptyList());
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

    /**
     * Expande cada oferta em aulas individuais.
     * Cada item da lista final vira uma variavel do CSP.
     */
    private List<AulaVariavel> expandirVariaveisCsp(
            List<TurmaDisciplina> ofertas,
            int capacidadeTurma,
            List<String> observacoes
    ) {
        List<AulaVariavel> variaveis = new ArrayList<>();
        int indiceGlobal = 1;

        for (TurmaDisciplina oferta : ofertas) {
            int carga = Optional.ofNullable(oferta.getCargaHorariaSemanal()).orElse(0);
            if (carga <= 0) {
                observacoes.add("Oferta TD " + oferta.getId() + " ignorada por carga horaria semanal invalida (<= 0).");
                continue;
            }

            for (int ordemNaDisciplina = 1; ordemNaDisciplina <= carga; ordemNaDisciplina++) {
                variaveis.add(new AulaVariavel(
                        indiceGlobal++,
                        ordemNaDisciplina,
                        oferta,
                        oferta.getProfessor().getId(),
                        oferta.getTurma().getId(),
                        capacidadeTurma,
                        Boolean.TRUE.equals(oferta.getDisciplina().getRequerLaboratorio())
                ));
            }
        }

        return variaveis;
    }

    private Map<Long, Set<Long>> carregarDisponibilidadePorProfessor(List<TurmaDisciplina> ofertas) {
        Set<Long> professorIds = ofertas.stream()
                .map(oferta -> oferta.getProfessor().getId())
                .collect(Collectors.toSet());

        List<DisponibilidadeProfessor> disponibilidades = disponibilidadeProfessorRepository.findByProfessorIdIn(professorIds);

        return disponibilidades.stream().collect(Collectors.groupingBy(
                disponibilidade -> disponibilidade.getProfessor().getId(),
                Collectors.mapping(disponibilidade -> disponibilidade.getSlotHorario().getId(), Collectors.toSet())
        ));
    }

    private void registrarAlertasDisponibilidade(
            List<TurmaDisciplina> ofertas,
            Map<Long, Set<Long>> disponibilidadePorProfessor,
            List<String> observacoes
    ) {
        Map<Long, String> professorNomePorId = new HashMap<>();
        for (TurmaDisciplina oferta : ofertas) {
            professorNomePorId.put(oferta.getProfessor().getId(), oferta.getProfessor().getNome());
        }

        for (Map.Entry<Long, String> professor : professorNomePorId.entrySet()) {
            Set<Long> slots = disponibilidadePorProfessor.getOrDefault(professor.getKey(), Collections.emptySet());
            if (slots.isEmpty()) {
                observacoes.add("Professor " + professor.getValue() + " (id=" + professor.getKey() + ") nao possui disponibilidade cadastrada.");
            }
        }
    }

    private Map<Long, List<Sala>> montarSalasPossiveisPorOferta(
            List<TurmaDisciplina> ofertas,
            int capacidadeTurma,
            List<Sala> salasFfd,
            List<String> observacoes
    ) {
        Map<Long, List<Sala>> resultado = new HashMap<>();

        for (TurmaDisciplina oferta : ofertas) {
            boolean requerLaboratorio = Boolean.TRUE.equals(oferta.getDisciplina().getRequerLaboratorio());

            List<Sala> salasPossiveis = salasFfd.stream()
                    .filter(sala -> capacidadeSegura(sala.getCapacidade()) >= capacidadeTurma)
                    .filter(sala -> !requerLaboratorio || salaEhLaboratorio(sala))
                    .toList();

            if (salasPossiveis.isEmpty()) {
                observacoes.add(
                        "Nenhuma sala compativel para TD " + oferta.getId()
                                + " (" + oferta.getDisciplina().getNome() + ")."
                                + " Capacidade da turma: " + capacidadeTurma
                                + ", requer laboratorio: " + requerLaboratorio + "."
                );
            }

            resultado.put(oferta.getId(), salasPossiveis);
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
        TurmaDisciplina td = alocacao.variavel().turmaDisciplina();
        return ScheduleAulaResponseDTO.builder()
                .indiceAula(alocacao.variavel().indiceGlobal())
                .turmaDisciplinaId(td.getId())
                .disciplinaId(td.getDisciplina().getId())
                .disciplinaNome(td.getDisciplina().getNome())
                .professorId(td.getProfessor().getId())
                .professorNome(td.getProfessor().getNome())
                .turmaId(td.getTurma().getId())
                .turmaNome(td.getTurma().getNome())
                .salaId(alocacao.sala().getId())
                .salaNome(alocacao.sala().getNome())
                .slotHorarioId(alocacao.slot().getId())
                .diaSemana(alocacao.slot().getDiaSemana())
                .horaInicio(alocacao.slot().getHoraInicio())
                .horaFim(alocacao.slot().getHoraFim())
                .build();
    }

    private ScheduleGenerationResponseDTO responseFalha(
            Turma turma,
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
            Turma turma,
            int totalNecessario,
            int totalAlocado,
            List<ScheduleAulaResponseDTO> aulas,
            List<String> observacoes
    ) {
        return ScheduleGenerationResponseDTO.builder()
                .sucesso(sucesso)
                .mensagem(mensagem)
                .turmaId(turma.getId())
                .turmaNome(turma.getNome())
                .totalAulasNecessarias(totalNecessario)
                .totalAulasAlocadas(totalAlocado)
                .aulas(aulas)
                .observacoes(observacoes)
                .build();
    }

    /**
     * Variavel do CSP: uma aula individual derivada de uma TurmaDisciplina.
     */
    private record AulaVariavel(
            int indiceGlobal,
            int ordemNaDisciplina,
            TurmaDisciplina turmaDisciplina,
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
