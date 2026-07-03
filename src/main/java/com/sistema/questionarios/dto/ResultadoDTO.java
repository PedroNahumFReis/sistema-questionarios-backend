package com.sistema.questionarios.dto;

import lombok.Data;

/**
 * DTO com o resultado da correção, devolvido ao aluno após responder (RF13).
 */
@Data
public class ResultadoDTO {
    private Double notaFinal;      // soma das pontuações das perguntas acertadas
    private Integer acertos;       // quantidade de respostas corretas
    private Integer erros;         // quantidade de respostas incorretas
    private Double percentual;     // aproveitamento em % (acertos / total * 100)
    private Long questionarioId;   // usado para montar o link HATEOAS de estatísticas na resposta

    public ResultadoDTO(Double notaFinal, Integer acertos, Integer erros, Double percentual, Long questionarioId) {
        this.notaFinal = notaFinal;
        this.acertos = acertos;
        this.erros = erros;
        this.percentual = percentual;
        this.questionarioId = questionarioId;
    }
}
