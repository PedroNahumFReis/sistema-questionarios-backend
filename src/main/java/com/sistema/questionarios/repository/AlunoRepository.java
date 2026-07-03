package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de Aluno. Herda os métodos CRUD prontos do JpaRepository.
 */
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    // Busca um aluno pelo e-mail (query derivada do nome do método).
    Optional<Aluno> findByEmail(String email);
}
