package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorDisciplinasItemDTO;
import br.com.timeforge.timeforge_api.dto.response.RelatorioProfessorDisciplinasResponseDTO;
import br.com.timeforge.timeforge_api.entity.Disciplina;
import br.com.timeforge.timeforge_api.entity.Professor;
import br.com.timeforge.timeforge_api.entity.ProfessorDisciplina;
import br.com.timeforge.timeforge_api.repository.ProfessorDisciplinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelatorioProfessorDisciplinasService {

    private final ProfessorDisciplinaRepository professorDisciplinaRepository;

    public RelatorioProfessorDisciplinasResponseDTO gerarRelatorio() {
        List<ProfessorDisciplina> vinculos =
                professorDisciplinaRepository.findAllByOrderByProfessor_NomeAscDisciplina_NomeAsc();

        Map<Long, Acumulador> acumuladores = new LinkedHashMap<>();
        int totalDisciplinasVinculadas = 0;

        for (ProfessorDisciplina vinculo : vinculos) {
            Professor professor = vinculo.getProfessor();
            Disciplina disciplina = vinculo.getDisciplina();
            if (professor == null || disciplina == null) {
                continue;
            }

            Acumulador acumulador = acumuladores.computeIfAbsent(
                    professor.getId(),
                    id -> new Acumulador(professor.getId(), professor.getNome())
            );

            acumulador.disciplinas.add(formatarDisciplina(disciplina));
            totalDisciplinasVinculadas += 1;
        }

        List<RelatorioProfessorDisciplinasItemDTO> itens = acumuladores.values().stream()
                .map(acumulador -> RelatorioProfessorDisciplinasItemDTO.builder()
                        .professorId(acumulador.professorId)
                        .professorNome(acumulador.professorNome)
                        .totalDisciplinas(acumulador.disciplinas.size())
                        .disciplinas(acumulador.disciplinas)
                        .build())
                .toList();

        return RelatorioProfessorDisciplinasResponseDTO.builder()
                .totalProfessores(itens.size())
                .totalDisciplinasVinculadas(totalDisciplinasVinculadas)
                .itens(itens)
                .build();
    }

    private String formatarDisciplina(Disciplina disciplina) {
        String nome = disciplina.getNome() == null || disciplina.getNome().isBlank()
                ? "-"
                : disciplina.getNome();
        String codigo = disciplina.getCodigo();

        if (codigo == null || codigo.isBlank()) {
            return nome;
        }

        return codigo + " - " + nome;
    }

    private static class Acumulador {
        private final Long professorId;
        private final String professorNome;
        private final List<String> disciplinas;

        private Acumulador(Long professorId, String professorNome) {
            this.professorId = professorId;
            this.professorNome = professorNome != null ? professorNome : "-";
            this.disciplinas = new ArrayList<>();
        }
    }
}
