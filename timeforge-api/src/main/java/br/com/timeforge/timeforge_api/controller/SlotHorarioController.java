package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.SlotHorarioRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.SlotHorarioResponseDTO;
import br.com.timeforge.timeforge_api.service.SlotHorarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slothorarios")
public class SlotHorarioController {

  private final SlotHorarioService service;

  public SlotHorarioController(SlotHorarioService service) {
    this.service = service;
  }

  @GetMapping
  public List<SlotHorarioResponseDTO> listarSlotHorarios() {
    return service.listarSlotHorarios();
  }

  @GetMapping("/{id}")
  public SlotHorarioResponseDTO listarSlotHorariosId(@PathVariable Long id) {
    return service.listarSlotHorariosId(id);
  }

  @PostMapping
  public SlotHorarioResponseDTO gravarSlotHorario(@RequestBody @Valid SlotHorarioRequestDTO payload) {
    return service.gravarSlotHorario(payload);
  }

  @PatchMapping("/{id}")
  public SlotHorarioResponseDTO editarSlotHorario(@PathVariable Long id, @RequestBody @Valid SlotHorarioRequestDTO payload) {
    return service.editarSlotHorario(id, payload);
  }

  @DeleteMapping("/{id}")
  public void deletarSlotHorario(@PathVariable Long id) {
    service.excluirSlotHorario(id);
  }
}