package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório de Questionario. Só o CRUD padrão do JpaRepository já é suficiente aqui.
 */
public interface QuestionarioRepository extends JpaRepository<Questionario, Long> {}
