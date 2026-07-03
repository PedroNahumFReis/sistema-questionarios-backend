package com.sistema.questionarios.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResultadoDTO {
    private Double notaFinal;
    private Integer acertos;
    private Integer erros;
    private Double percentual;

    public ResultadoDTO(Double notaFinal, Integer acertos, Integer erros, Double percentual) {
        this.notaFinal = notaFinal;
        this.acertos = acertos;
        this.erros = erros;
        this.percentual = percentual;
    }
}