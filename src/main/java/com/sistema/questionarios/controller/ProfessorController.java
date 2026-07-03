package com.sistema.questionarios.controller;

import com.sistema.questionarios.model.Professor;
import com.sistema.questionarios.repository.ProfessorRepository;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller de Professor (RF01).
 */
@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorRepository repository;
    private final PasswordEncoder passwordEncoder; // usado para criptografar a senha

    public ProfessorController(ProfessorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /api/professores — cadastra um professor.
     * A senha é criptografada com BCrypt antes de ir para o banco (nunca guardamos senha em texto puro).
     */
    @PostMapping
    public ResponseEntity<EntityModel<Professor>> cadastrar(@RequestBody Professor professor) {
        professor.setSenha(passwordEncoder.encode(professor.getSenha()));
        Professor salvo = repository.save(professor);
        return ResponseEntity.ok(toResource(salvo));
    }

    /**
     * GET /api/professores/{id} — busca um professor pelo id.
     * Existe também para servir de alvo do link "self" do HATEOAS (RF17).
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Professor>> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(professor -> ResponseEntity.ok(toResource(professor)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * RF17: envolve o professor em um EntityModel com o link "self".
     * Usa uma CÓPIA sem a senha por dois motivos: (1) não vazar o hash da senha na resposta;
     * (2) não arriscar que o Open-Session-In-View persista uma alteração na entidade gerenciada.
     */
    private EntityModel<Professor> toResource(Professor professor) {
        Professor copia = new Professor();
        copia.setId(professor.getId());
        copia.setNome(professor.getNome());
        copia.setEmail(professor.getEmail());
        // senha deixada como null de propósito

        EntityModel<Professor> resource = EntityModel.of(copia);
        resource.add(linkTo(methodOn(ProfessorController.class).buscar(copia.getId())).withSelfRel());
        return resource;
    }
}
