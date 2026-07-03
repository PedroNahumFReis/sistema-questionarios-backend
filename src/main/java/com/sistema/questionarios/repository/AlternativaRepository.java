package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório de Alternativa. Usado na correção para buscar cada alternativa marcada pelo aluno.
 */
public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {}
