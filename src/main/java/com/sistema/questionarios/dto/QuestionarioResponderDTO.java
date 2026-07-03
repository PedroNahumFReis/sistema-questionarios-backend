package com.sistema.questionarios.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO que entrega o questionário ao ALUNO para ele responder (usado pelo frontend).
 *
 * Diferença importante para a entidade Pergunta/Alternativa: aqui NÃO existe o campo 'correta'.
 * Assim o aluno recebe as perguntas e opções, mas nunca descobre a resposta certa pelo JSON.
 */
@Data
public class QuestionarioResponderDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private List<PerguntaResponder> perguntas;

    public QuestionarioResponderDTO(Long id, String titulo, String descricao, List<PerguntaResponder> perguntas) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.perguntas = perguntas;
    }

    /** Pergunta na visão do aluno: enunciado, tipo e as alternativas (sem indicar a correta). */
    @Data
    public static class PerguntaResponder {
        private Long id;
        private String enunciado;
        private String tipo;
        private List<AlternativaResponder> alternativas;

        public PerguntaResponder(Long id, String enunciado, String tipo, List<AlternativaResponder> alternativas) {
            this.id = id;
            this.enunciado = enunciado;
            this.tipo = tipo;
            this.alternativas = alternativas;
        }
    }

    /** Alternativa na visão do aluno: apenas id e texto. */
    @Data
    public static class AlternativaResponder {
        private Long id;
        private String texto;

        public AlternativaResponder(Long id, String texto) {
            this.id = id;
            this.texto = texto;
        }
    }
}
