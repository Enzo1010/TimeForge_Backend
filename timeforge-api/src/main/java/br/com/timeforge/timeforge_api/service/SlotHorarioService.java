package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.domain.SlotHorario;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class SlotHorarioService {
//
  private final SlotHorarioRepository repository;

  SlotHorarioService(SlotHorarioRepository repository) {
    this.repository = repository;
  }

  public List<SlotHorario> listarHorarios(){
    return    repository.findAll();
  }

}
