package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.domain.Professor;
import br.com.timeforge.timeforge_api.service.ProfessorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @GetMapping("/professores")
    public List<Professor> listarProfessores() {
        return service.listarProfessores();
    }
}