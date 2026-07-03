package com.sistema.questionarios.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO com as estatísticas de um questionário (RF15).
 *
 * Traz os números gerais (total, corretas, incorretas e percentuais) e um detalhamento
 * por pergunta na lista 'porPergunta'.
 */
@Data
public class EstatisticaDTO {
    private Long questionarioId;
    private long totalRespostas;
    private long respostasCorretas;
    private long respostasIncorretas;
    private double percentualAcertos;
    private double percentualErros;
    private List<EstatisticaPergunta> porPergunta;

    public EstatisticaDTO(Long questionarioId, long totalRespostas, long respostasCorretas,
                          long respostasIncorretas, double percentualAcertos, double percentualErros,
                          List<EstatisticaPergunta> porPergunta) {
        this.questionarioId = questionarioId;
        this.totalRespostas = totalRespostas;
        this.respostasCorretas = respostasCorretas;
        this.respostasIncorretas = respostasIncorretas;
        this.percentualAcertos = percentualAcertos;
        this.percentualErros = percentualErros;
        this.porPergunta = porPergunta;
    }

    /**
     * Classe interna (static): estatísticas de UMA pergunta específica.
     * Fica aninhada aqui por ser usada somente dentro de EstatisticaDTO.
     */
    @Data
    public static class EstatisticaPergunta {
        private Long perguntaId;
        private String enunciado;
        private long respostasCorretas;
        private long respostasIncorretas;
        private double percentualAcertos;
        private double percentualErros;

        public EstatisticaPergunta(Long perguntaId, String enunciado, long respostasCorretas,
                                   long respostasIncorretas, double percentualAcertos, double percentualErros) {
            this.perguntaId = perguntaId;
            this.enunciado = enunciado;
            this.respostasCorretas = respostasCorretas;
            this.respostasIncorretas = respostasIncorretas;
            this.percentualAcertos = percentualAcertos;
            this.percentualErros = percentualErros;
        }
    }
}
