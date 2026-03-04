package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.domain.Professor;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    public List<Professor> listarProfessores() {
        return repository.findAll();
    }
}