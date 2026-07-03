package com.sistema.questionarios.dto;

import lombok.Data;

/**
 * DTO para exibir uma resposta dada pelo aluno (RF14).
 *
 * Traz apenas os campos úteis para exibição (sem expor a entidade RespostaAluno inteira nem
 * as relações internas): a pergunta, a alternativa escolhida e se ela estava correta.
 */
@Data
public class RespostaViewDTO {
    private Long perguntaId;
    private String enunciado;
    private Long alternativaSelecionadaId;
    private String textoAlternativa;
    private Boolean correta;

    public RespostaViewDTO(Long perguntaId, String enunciado, Long alternativaSelecionadaId,
                           String textoAlternativa, Boolean correta) {
        this.perguntaId = perguntaId;
        this.enunciado = enunciado;
        this.alternativaSelecionadaId = alternativaSelecionadaId;
        this.textoAlternativa = textoAlternativa;
        this.correta = correta;
    }
}
