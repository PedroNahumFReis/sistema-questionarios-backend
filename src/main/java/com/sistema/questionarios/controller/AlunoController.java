package com.sistema.questionarios.controller;

import com.sistema.questionarios.model.Aluno;
import com.sistema.questionarios.repository.AlunoRepository;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller de Aluno (RF02).
 */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoRepository repository;

    public AlunoController(AlunoRepository repository) {
        this.repository = repository;
    }

    /**
     * POST /api/alunos — cadastra um aluno.
     */
    @PostMapping
    public ResponseEntity<EntityModel<Aluno>> cadastrar(@RequestBody Aluno aluno) {
        Aluno salvo = repository.save(aluno);
        return ResponseEntity.ok(toResource(salvo));
    }

    /**
     * GET /api/alunos/{id} — busca um aluno pelo id (também serve de alvo do link "self").
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Aluno>> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(aluno -> ResponseEntity.ok(toResource(aluno)))
                .orElse(ResponseEntity.notFound().build());
    }

    // RF17: envolve o aluno em um EntityModel com o link "self" apontando para o GET acima.
    private EntityModel<Aluno> toResource(Aluno aluno) {
        EntityModel<Aluno> resource = EntityModel.of(aluno);
        resource.add(linkTo(methodOn(AlunoController.class).buscar(aluno.getId())).withSelfRel());
        return resource;
    }
}
