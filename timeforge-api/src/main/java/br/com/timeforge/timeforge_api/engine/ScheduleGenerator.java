package br.com.timeforge.timeforge_api.engine;

import br.com.timeforge.timeforge_api.domain.TurmaDisciplina;
import br.com.timeforge.timeforge_api.repository.TurmaDisciplinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleGenerator {

    /*Isso não gera horário ainda. Só prova que:
        o endpoint chama o engine
        o engine lê do banco
        o engine transforma carga horária em variáveis do CSP
*/
    private final TurmaDisciplinaRepository turmaDisciplinaRepository;

    public void gerarHorario(Long turmaId) {
        List<TurmaDisciplina> ofertas = turmaDisciplinaRepository.findByTurmaId(turmaId);

        List<String> aulasParaAlocar = new ArrayList<>();

        for (TurmaDisciplina td : ofertas) {
            Integer carga = td.getCargaHorariaSemanal();
            for (int i = 1; i <= carga; i++) {
                aulasParaAlocar.add("TD-" + td.getId() + "#" + i);
            }
        }

        System.out.println("Turma " + turmaId + " -> aulas para alocar: " + aulasParaAlocar.size());
        System.out.println(aulasParaAlocar);
    }
}