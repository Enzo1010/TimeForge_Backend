package br.com.timeforge.timeforge_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "configuracao_gerador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoGerador {

    @Id
    private Long id;

    @Column(name = "limite_iteracoes", nullable = false)
    private Integer limiteIteracoes;

    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs;

    @Column(name = "max_aulas_professor_dia", nullable = false)
    private Integer maxAulasProfessorDia;

    @Column(name = "max_aulas_turma_dia", nullable = false)
    private Integer maxAulasTurmaDia;
}
