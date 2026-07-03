package com.sistema.questionarios.controller;

import com.sistema.questionarios.model.Aluno;
import com.sistema.questionarios.repository.AlunoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoRepository repository;

    public AlunoController(AlunoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Aluno> cadastrar(@RequestBody Aluno aluno) {
        Aluno salvo = repository.save(aluno);
        return ResponseEntity.ok(salvo);
    }
}