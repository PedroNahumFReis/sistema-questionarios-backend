package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade TokenAcesso (RF08).
 *
 * Representa a permissão temporária de um aluno responder um questionário específico.
 * Liga um aluno a um questionário e guarda a data de expiração e se já foi usado.
 */
@Data
@Entity
public class TokenAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Valor único do token (RF08). UUID.randomUUID() gera uma string aleatória e praticamente
    // impossível de adivinhar, já preenchida na criação do objeto. É ela que vai no link do e-mail.
    @Column(unique = true)
    private String token = UUID.randomUUID().toString();

    // Momento em que o token deixa de ser válido (RN03). Calculado no envio a partir de tempoValidadeTokenMinutos.
    private LocalDateTime dataExpiracao;

    // Controla a RN04 (responder apenas uma vez): vira true após a primeira resposta.
    private Boolean utilizado = false;

    // A qual aluno este token pertence (validação de acesso).
    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    // A qual questionário este token dá acesso.
    @ManyToOne
    @JoinColumn(name = "questionario_id")
    private Questionario questionario;
}
