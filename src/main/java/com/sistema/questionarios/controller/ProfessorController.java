package com.sistema.questionarios.controller;

import com.sistema.questionarios.model.Professor;
import com.sistema.questionarios.repository.ProfessorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorController(ProfessorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<Professor> cadastrar(@RequestBody Professor professor) {
        // Criptografando a senha antes de salvar no banco
        professor.setSenha(passwordEncoder.encode(professor.getSenha()));
        Professor salvo = repository.save(professor);
        return ResponseEntity.ok(salvo);
    }
}