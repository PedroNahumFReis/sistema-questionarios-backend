package com.sistema.questionarios.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilitário para criar e validar tokens JWT (RF03/RNF04).
 *
 * JWT é um "crachá" assinado digitalmente. O servidor gera no login e o cliente reenvia
 * a cada requisição. Como é assinado com uma chave secreta, ninguém consegue forjar ou
 * alterar o conteúdo sem invalidar a assinatura.
 *
 * @Component: o Spring cria e gerencia uma instância desta classe para injetar onde for preciso.
 */
@Component
public class JwtUtil {

    // Valores lidos do application.properties (jwt.secret e jwt.expiration).
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Transforma a string secreta em uma chave criptográfica para assinar/verificar os tokens.
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Gera um token para o e-mail informado. Guarda quem é (subject), quando foi emitido
    // e quando expira, e assina tudo com a chave secreta usando o algoritmo HS256.
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lê o e-mail (subject) de dentro do token. Só funciona se a assinatura for válida.
    public String extrairEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Retorna true se o token é autêntico e não expirou; false se está adulterado, vencido ou malformado.
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
