package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.dto.request.TurmaDisciplinaRequestDTO;
import br.com.timeforge.timeforge_api.dto.response.TurmaDisciplinaResponseDTO;
import br.com.timeforge.timeforge_api.service.TurmaDisciplinaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/turmas-disciplinas")
@RestController
@Validated
public class TurmaDisciplinaController {

    private final TurmaDisciplinaService service;

    public TurmaDisciplinaController(TurmaDisciplinaService service) {
        this.service = service;
    }

    @GetMapping()
    public List<TurmaDisciplinaResponseDTO> listarTurmasDisciplinas() {
        return service.listarTurmasDisciplinas();
    }

    @GetMapping("/{id}")
    public TurmaDisciplinaResponseDTO listarTurmaDisciplinaId(
            @PathVariable @Positive(message = "id deve ser maior que zero") Long id
    ) {
        return service.listarTurmaDisciplinaId(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public TurmaDisciplinaResponseDTO cadastrarTurmaDisciplina(
            @RequestBody @Valid TurmaDisciplinaRequestDTO payload
    ) {
        return service.cadastrarTurmaDisciplina(payload);
    }

    @PutMapping("/{id}")
    public TurmaDisciplinaResponseDTO editarTurmaDisciplina(
            @PathVariable @Positive(message = "id deve ser maior que zero") Long id,
            @RequestBody @Valid TurmaDisciplinaRequestDTO payload
    ) {
        return service.editarTurmaDisciplina(id, payload);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarTurmaDisciplina(@PathVariable @Positive(message = "id deve ser maior que zero") Long id) {
        service.deletarTurmaDisciplina(id);
    }
}
