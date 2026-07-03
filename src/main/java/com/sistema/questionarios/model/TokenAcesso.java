package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class TokenAcesso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token = UUID.randomUUID().toString(); // RF08: Token Único

    private LocalDateTime dataExpiracao;
    private Boolean utilizado = false; // RN04: Respondido apenas uma vez

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "questionario_id")
    private Questionario questionario;
}