package br.com.timeforge.timeforge_api.repository;

import br.com.timeforge.timeforge_api.entity.ConfiguracaoGerador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoGeradorRepository extends JpaRepository<ConfiguracaoGerador, Long> {
}
