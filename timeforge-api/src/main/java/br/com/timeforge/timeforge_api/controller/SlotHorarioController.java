package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.SlotHorarioRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.SlotHorarioResponseDTO;
import br.com.timeforge.timeforge_api.service.SlotHorarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slothorarios")
@Validated
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
  public SlotHorarioResponseDTO listarSlotHorariosId(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    return service.listarSlotHorariosId(id);
  }

  @PostMapping
  public SlotHorarioResponseDTO gravarSlotHorario(@RequestBody @Valid SlotHorarioRequestDTO payload) {
    return service.gravarSlotHorario(payload);
  }

  @PutMapping("/{id}")
  public SlotHorarioResponseDTO editarSlotHorario(@PathVariable @Positive(message = "id deve ser maior que zero") Long id, @RequestBody @Valid SlotHorarioRequestDTO payload) {
    return service.editarSlotHorario(id, payload);
  }

  @DeleteMapping("/{id}")
  public void deletarSlotHorario(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
    service.excluirSlotHorario(id);
  }
}
