package com.sistema.questionarios.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração central de segurança (RNF04).
 *
 * Define quais rotas são públicas e quais exigem login, a política de sessão e onde
 * o filtro JWT entra na cadeia de filtros do Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Nosso filtro JWT, injetado pelo Spring para ser encaixado na cadeia de segurança.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Um @Bean é um objeto gerenciado pelo Spring. Aqui definimos as regras de segurança da aplicação.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF é uma proteção para apps com sessão/formulários. Como usamos API REST com JWT
                // (sem sessão/cookies), desabilitar é o padrão recomendado.
                .csrf(csrf -> csrf.disable())
                // STATELESS: o servidor não guarda sessão; cada requisição se autentica sozinha pelo JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas (não exigem login):
                        .requestMatchers(
                                "/api/auth/**",        // login (para conseguir o token)
                                "/api/professores",    // cadastro de professor (RF01)
                                "/swagger-ui/**",      // documentação Swagger (RF16)
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui.html",
                                "/error"               // página padrão de erro
                        ).permitAll()
                        // Aluno responde sem login, usando o token do e-mail (RF11).
                        .requestMatchers(HttpMethod.POST, "/api/responder/**").permitAll()
                        // Aluno vê as próprias respostas sem login, também pelo token (RF14).
                        .requestMatchers(HttpMethod.GET, "/api/respostas/**").permitAll()
                        // Qualquer outra rota exige estar autenticado (ex.: criar questionário - RN01).
                        .anyRequest().authenticated()
                )
                // Coloca nosso filtro JWT ANTES do filtro padrão de usuário/senha, para que a
                // autenticação por token já esteja pronta quando o Spring for decidir o acesso.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean padrão do Spring Security usado para autenticação (disponível caso precise ser injetado).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Algoritmo de hash das senhas. BCrypt gera um hash com "sal" aleatório, seguro para guardar senhas.
    // É usado ao cadastrar o professor e ao conferir a senha no login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
