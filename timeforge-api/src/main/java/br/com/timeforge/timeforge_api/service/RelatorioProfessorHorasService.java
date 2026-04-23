package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorHorasItemDTO;
import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorHorasResponseDTO;
import br.com.timeforge.timeforge_api.entity.Aula;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.SlotHorario;
import br.com.timeforge.timeforge_api.repository.AulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelatorioProfessorHorasService {

    private final AulaRepository aulaRepository;

    public RelatorioProfessorHorasResponseDTO gerarRelatorio() {
        List<Aula> aulas = aulaRepository
                .findAllByOrderByProfessor_NomeAscSlotHorario_DiaSemanaAscSlotHorario_HoraInicioAscSlotHorario_HoraFimAsc();

        Map<Long, Acumulador> acumuladores = new LinkedHashMap<>();
        int totalAulas = 0;
        int totalMinutos = 0;

        for (Aula aula : aulas) {
            Professor professor = aula.getProfessor();
            SlotHorario slot = aula.getSlotHorario();
            if (professor == null || slot == null) {
                continue;
            }

            int minutos = calcularMinutos(slot.getHoraInicio(), slot.getHoraFim());
            totalAulas += 1;
            totalMinutos += minutos;

            Acumulador acumulador = acumuladores.computeIfAbsent(
                    professor.getId(),
                    id -> new Acumulador(professor.getId(), professor.getNome())
            );

            acumulador.totalAulas += 1;
            acumulador.totalMinutos += minutos;
        }

        List<RelatorioProfessorHorasItemDTO> itens = acumuladores.values().stream()
                .map(ac -> RelatorioProfessorHorasItemDTO.builder()
                        .professorId(ac.professorId)
                        .professorNome(ac.professorNome)
                        .totalAulas(ac.totalAulas)
                        .totalMinutos(ac.totalMinutos)
                        .totalHorasFormatadas(formatarMinutos(ac.totalMinutos))
                        .build())
                .toList();

        return RelatorioProfessorHorasResponseDTO.builder()
                .totalProfessores(itens.size())
                .totalAulas(totalAulas)
                .totalMinutos(totalMinutos)
                .totalHorasFormatadas(formatarMinutos(totalMinutos))
                .itens(itens)
                .build();
    }

    private int calcularMinutos(LocalTime inicio, LocalTime fim) {
        if (inicio == null || fim == null) {
            return 0;
        }
        long minutos = Duration.between(inicio, fim).toMinutes();
        if (minutos < 0) {
            return 0;
        }
        return (int) minutos;
    }

    private String formatarMinutos(int totalMinutos) {
        int horas = totalMinutos / 60;
        int minutos = totalMinutos % 60;
        return String.format("%02d:%02d", horas, minutos);
    }

    private static class Acumulador {
        private final Long professorId;
        private final String professorNome;
        private int totalAulas;
        private int totalMinutos;

        private Acumulador(Long professorId, String professorNome) {
            this.professorId = professorId;
            this.professorNome = professorNome != null ? professorNome : "-";
            this.totalAulas = 0;
            this.totalMinutos = 0;
        }
    }
}
