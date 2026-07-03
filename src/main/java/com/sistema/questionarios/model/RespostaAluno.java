package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade RespostaAluno.
 *
 * Guarda cada alternativa que o aluno escolheu ao responder um questionário.
 * É a persistência que torna possíveis o RF14 (ver as respostas) e o RF15 (estatísticas).
 * Uma linha desta tabela = uma alternativa marcada pelo aluno em uma pergunta.
 */
@Data
@Entity
public class RespostaAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token usado para responder. A partir dele chegamos ao aluno e ao questionário
    // (por isso não precisamos repetir esses campos aqui).
    @ManyToOne
    @JoinColumn(name = "token_id")
    private TokenAcesso token;

    // A qual pergunta esta resposta se refere.
    @ManyToOne
    @JoinColumn(name = "pergunta_id")
    private Pergunta pergunta;

    // Qual alternativa o aluno escolheu.
    @ManyToOne
    @JoinColumn(name = "alternativa_id")
    private Alternativa alternativaSelecionada;

    // Se a alternativa escolhida estava correta (calculado na correção; evita recontar depois).
    private Boolean correta;
}
