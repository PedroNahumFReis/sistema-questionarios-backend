package com.sistema.questionarios.model;

/**
 * Tipos possíveis de uma pergunta (RF05).
 *
 * Um enum limita os valores válidos: o campo 'tipo' da Pergunta só pode assumir uma destas opções,
 * evitando texto livre inconsistente no banco.
 */
public enum TipoPergunta {
    UNICA_ESCOLHA,     // apenas uma alternativa correta
    MULTIPLA_ESCOLHA,  // pode haver mais de uma correta
    VERDADEIRO_FALSO   // duas alternativas: verdadeiro/falso
}
