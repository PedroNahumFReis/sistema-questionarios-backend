package com.sistema.questionarios.repository;

import com.sistema.questionarios.model.TokenAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenAcessoRepository extends JpaRepository<TokenAcesso, Long> {
    Optional<TokenAcesso> findByToken(String token);
}
