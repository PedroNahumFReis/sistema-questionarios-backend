package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {}
