package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.domain.SlotHorario;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;
import org.hibernate.annotations.NotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class SlotHorarioService {
//
  private final SlotHorarioRepository repository;

  public SlotHorarioService(SlotHorarioRepository repository) {
    this.repository = repository;
  }

  public List<SlotHorario> listarSlotHorarios(){
    return repository.findAll();
  }

  public SlotHorario gravarSlotHorario(@RequestBody SlotHorario slotHorarioObject){
    return repository.save(slotHorarioObject);
  }

  public SlotHorario editarSlotHorario(Long id, SlotHorario slotHorarioObject){
    SlotHorario slotHorarioEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot de horario com id (" + id + ") não encontrado"));

    slotHorarioEncontrado.setDiaSemana(slotHorarioObject.getDiaSemana());
    slotHorarioEncontrado.setHoraInicio(slotHorarioObject.getHoraInicio());
    slotHorarioEncontrado.setHoraFim(slotHorarioObject.getHoraFim());

    return repository.save(slotHorarioEncontrado);
  }

  public ResponseEntity<String> excluirSlotHorario(Long id){
    SlotHorario slotHorarioEncontrado = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot de horario com id (" + id + ") não encontrado"));

    repository.delete(slotHorarioEncontrado);

    return ResponseEntity.ok("Slot de horario excluido com sucesso!");
  }

}
