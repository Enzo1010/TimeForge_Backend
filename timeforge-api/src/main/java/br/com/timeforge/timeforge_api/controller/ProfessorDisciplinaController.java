package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.ProfessorDisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.ProfessorDisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.service.ProfessorDisciplinaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professor-disciplinas")
@Validated
public class ProfessorDisciplinaController {

    private final ProfessorDisciplinaService service;

    public ProfessorDisciplinaController(ProfessorDisciplinaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfessorDisciplinaResponseDTO> listar(
            @RequestParam(required = false) Long professorId,
            @RequestParam(required = false) Long disciplinaId
    ) {
        if (professorId != null) {
            return service.listarPorProfessor(professorId);
        }
        if (disciplinaId != null) {
            return service.listarPorDisciplina(disciplinaId);
        }
        return List.of();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessorDisciplinaResponseDTO adicionar(@RequestBody @Valid ProfessorDisciplinaRequestDTO dto) {
        return service.adicionar(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
        service.remover(id);
    }
}
