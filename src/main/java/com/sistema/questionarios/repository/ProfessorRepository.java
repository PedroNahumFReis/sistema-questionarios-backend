package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de Professor (camada de acesso a dados).
 *
 * Ao estender JpaRepository<Professor, Long>, o Spring Data JPA já fornece prontos
 * os métodos save, findById, findAll, delete etc. — não precisamos implementá-los.
 * O <Professor, Long> diz: entidade Professor, cujo id é do tipo Long.
 */
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    // Query derivada: o Spring cria a consulta "SELECT ... WHERE email = ?" só pelo nome do método.
    // Usada no login (RF03) para achar o professor pelo e-mail. Optional evita NullPointerException.
    Optional<Professor> findByEmail(String email);
}
