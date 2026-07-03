package com.sistema.questionarios.controller;

import com.sistema.questionarios.dto.LoginDTO;
import com.sistema.questionarios.model.Professor;
import com.sistema.questionarios.repository.ProfessorRepository;
import com.sistema.questionarios.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de autenticação (RF03).
 *
 * @RestController: cada método retorna diretamente o corpo da resposta (JSON), sem página HTML.
 * @RequestMapping define o prefixo "/api/auth" comum a todas as rotas desta classe.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;                     // gera o token JWT
    private final ProfessorRepository professorRepository; // busca o professor no banco
    private final PasswordEncoder passwordEncoder;     // confere a senha (BCrypt)

    public AuthController(JwtUtil jwtUtil,
                          ProfessorRepository professorRepository,
                          PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /api/auth/login — recebe e-mail e senha e, se estiverem corretos, devolve um token JWT.
     * @RequestBody converte o JSON recebido em um objeto LoginDTO.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        // Busca o professor pelo e-mail. Se não existir, 'professor' fica null.
        Professor professor = professorRepository.findByEmail(loginDTO.getEmail()).orElse(null);

        // matches() compara a senha digitada com o hash salvo. Se não bater (ou não existir o professor),
        // devolve 401 (não autorizado). Nunca revelamos qual dos dois falhou, por segurança.
        if (professor == null || !passwordEncoder.matches(loginDTO.getSenha(), professor.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "E-mail ou senha inválidos."));
        }

        // Credenciais válidas: gera e devolve o token, que o cliente usará nos próximos acessos.
        String token = jwtUtil.generateToken(professor.getEmail());
        return ResponseEntity.ok(Map.of("token", token, "tipo", "Bearer"));
    }
}
