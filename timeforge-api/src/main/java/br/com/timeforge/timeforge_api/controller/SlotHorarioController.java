package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.domain.SlotHorario;
import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import br.com.timeforge.timeforge_api.service.SlotHorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slothorarios")
public class SlotHorarioController {

  private final SlotHorarioService service;

  public SlotHorarioController(SlotHorarioService service) {
    this.service = service;
  }

  @GetMapping("/listar")
  public List<SlotHorario> listarSlotHorarios(){
    return service.listarSlotHorarios();
  }

  @PostMapping("/gravar")
  public SlotHorario gravarSlotHorario(@RequestBody SlotHorario slotHorarioObject){
    return service.gravarSlotHorario(slotHorarioObject);
  }

  @PatchMapping("/editar/{id}")
  public SlotHorario editarSlotHorario(@PathVariable Long id, @RequestBody SlotHorario slotHorarioObject){
    return service.editarSlotHorario(id, slotHorarioObject);
  }

  @DeleteMapping("/deletar/{id}")
  public ResponseEntity<String> deletarSlotHorario(@PathVariable Long id){
    service.excluirSlotHorario(id);
    return ResponseEntity.ok("Slot de horario excluido com sucesso!");
  }


}
