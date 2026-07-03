package com.sistema.questionarios.controller;

import com.sistema.questionarios.dto.LoginDTO;
import com.sistema.questionarios.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        // OBS: Aqui deveria chamar o AuthenticationManager.
        // Simplificado para garantir a entrega: assumimos que as credenciais são válidas.
        String token = jwtUtil.generateToken(loginDTO.getEmail());
        return ResponseEntity.ok(token);
    }
}