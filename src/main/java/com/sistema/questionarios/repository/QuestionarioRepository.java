package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuestionarioRepository extends JpaRepository<Questionario, Long> {}

