package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório de Pergunta. Usado para persistir perguntas adicionadas a um questionário (RF05).
 */
public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {}
