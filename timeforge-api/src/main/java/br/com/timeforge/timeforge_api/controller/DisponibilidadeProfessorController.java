package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.DisponibilidadeProfessorRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.DisponibilidadeProfessorResponseDTO;
import br.com.timeforge.timeforge_api.service.DisponibilidadeProfessorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/disponibilidades-professor")
@RestController
@Validated
public class DisponibilidadeProfessorController {

    private final DisponibilidadeProfessorService service;

    public DisponibilidadeProfessorController(DisponibilidadeProfessorService service) {
        this.service = service;
    }

    @GetMapping()
    public List<DisponibilidadeProfessorResponseDTO> listarDisponibilidadesProfessor() {
        return service.listarDisponibilidadesProfessor();
    }

    @GetMapping("/{id}")
    public DisponibilidadeProfessorResponseDTO listarDisponibilidadeProfessorId(
            @PathVariable @Positive(message = "id deve ser maior que zero") Long id
    ) {
        return service.listarDisponibilidadeProfessorId(id);
    }

    @PostMapping()
    public DisponibilidadeProfessorResponseDTO cadastrarDisponibilidadeProfessor(
            @RequestBody @Valid DisponibilidadeProfessorRequestDTO payload
    ) {
        return service.cadastrarDisponibilidadeProfessor(payload);
    }

    @PutMapping("/{id}")
    public DisponibilidadeProfessorResponseDTO editarDisponibilidadeProfessor(
            @PathVariable @Positive(message = "id deve ser maior que zero") Long id,
            @RequestBody @Valid DisponibilidadeProfessorRequestDTO payload
    ) {
        return service.editarDisponibilidadeProfessor(id, payload);
    }

    @DeleteMapping("/{id}")
    public void deletarDisponibilidadeProfessor(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
        service.deletarDisponibilidadeProfessor(id);
    }
}
