package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.dto.response.RelatorioSalasCapacidadeItemDTO;
import br.com.timeforge.timeforge_api.dto.response.RelatorioSalasCapacidadeResponseDTO;
import br.com.timeforge.timeforge_api.entity.Sala;
import br.com.timeforge.timeforge_api.entity.TipoSala;
import br.com.timeforge.timeforge_api.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioSalasCapacidadeService {

    private final SalaRepository salaRepository;

    public RelatorioSalasCapacidadeResponseDTO gerarRelatorio() {
        List<Sala> salas = salaRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));

        int capacidadeTotal = salas.stream()
                .map(Sala::getCapacidade)
                .filter(capacidade -> capacidade != null && capacidade > 0)
                .mapToInt(Integer::intValue)
                .sum();

        List<RelatorioSalasCapacidadeItemDTO> itens = salas.stream()
                .map(sala -> RelatorioSalasCapacidadeItemDTO.builder()
                        .salaId(sala.getId())
                        .salaNome(safe(sala.getNome()))
                        .capacidade(sala.getCapacidade() != null ? sala.getCapacidade() : 0)
                        .tipoSala(formatarTipoSala(sala.getTipoSala()))
                        .build())
                .toList();

        int capacidadeMedia = itens.isEmpty() ? 0 : capacidadeTotal / itens.size();

        return RelatorioSalasCapacidadeResponseDTO.builder()
                .totalSalas(itens.size())
                .capacidadeTotal(capacidadeTotal)
                .capacidadeMedia(capacidadeMedia)
                .itens(itens)
                .build();
    }

    private String formatarTipoSala(TipoSala tipoSala) {
        if (tipoSala == null) {
            return "Comum";
        }

        return switch (tipoSala) {
            case LABORATORIO -> "Laboratorio";
            case COMUM -> "Comum";
        };
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
