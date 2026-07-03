package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.TokenAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de TokenAcesso.
 */
public interface TokenAcessoRepository extends JpaRepository<TokenAcesso, Long> {

    // Busca o token pela string enviada no link do e-mail. É o primeiro passo da validação (RF10)
    // quando o aluno tenta responder o questionário.
    Optional<TokenAcesso> findByToken(String token);
}
