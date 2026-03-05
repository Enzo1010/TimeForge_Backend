package br.com.timeforge.timeforge_api.service;

import br.com.timeforge.timeforge_api.domain.Professor;
import br.com.timeforge.timeforge_api.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    public Professor listarProfessorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor com id (" + id + ") não encontrado!"));
    }

    public Professor cadastrarProfessor(Professor professorObject) {
        return repository.save(professorObject);
    }

    public Professor editarProfessor(Long id, Professor professorObject){
        Professor professorEncontrado = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado!"));

        professorEncontrado.setNome(professorObject.getNome());

        return repository.save(professorEncontrado);
    }

    public void deletarProfessor(Long id){
        Professor professorEncontrado = repository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Professor não encontrado!"));

        repository.delete(professorEncontrado);
    }
}