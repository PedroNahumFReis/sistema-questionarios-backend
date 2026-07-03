package com.sistema.questionarios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que intercepta TODA requisição para validar o token JWT (RNF04).
 *
 * Estende OncePerRequestFilter para garantir que roda exatamente uma vez por requisição.
 * Se o token no cabeçalho "Authorization: Bearer ..." for válido, ele marca o usuário como
 * autenticado no SecurityContext; a partir daí o Spring Security libera os endpoints protegidos.
 * Sem este filtro, nenhuma requisição conseguiria se autenticar e tudo cairia em 401/403.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Lê o cabeçalho onde o cliente envia o token.
        String authHeader = request.getHeader("Authorization");

        // Só processa se vier no formato "Bearer <token>".
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // remove o prefixo "Bearer "

            // Valida o token e evita reautenticar se já houver autenticação neste contexto.
            if (jwtUtil.validarToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.extrairEmail(token);

                // Cria o objeto de autenticação do Spring: quem é o usuário (email), sem senha,
                // e com a permissão (role) PROFESSOR.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_PROFESSOR")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Registra o usuário como autenticado para o restante do processamento desta requisição.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Passa a requisição adiante (para o próximo filtro ou para o controller).
        filterChain.doFilter(request, response);
    }
}
