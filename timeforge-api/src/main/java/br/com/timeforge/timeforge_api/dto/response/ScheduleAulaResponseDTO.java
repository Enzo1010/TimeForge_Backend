package br.com.timeforge.timeforge_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Representa uma aula individual na grade gerada em memória.
 * Cada item descreve exatamente uma alocação válida: disciplina + professor + turma + sala + slot.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleAulaResponseDTO {

    private Integer indiceAula;
    private Long turmaDisciplinaId;

    private Long disciplinaId;
    private String disciplinaNome;

    private Long professorId;
    private String professorNome;

    private Long turmaId;
    private String turmaNome;

    private Long salaId;
    private String salaNome;

    private Long slotHorarioId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
}
