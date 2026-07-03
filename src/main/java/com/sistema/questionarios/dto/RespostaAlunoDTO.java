package com.sistema.questionarios.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO que o aluno envia ao responder o questionário (RF11).
 *
 * Contém apenas os ids das alternativas que ele marcou. O servidor usa esses ids
 * para buscar as alternativas e fazer a correção automática (RF12).
 */
@Data
public class RespostaAlunoDTO {
    private List<Long> idsAlternativasSelecionadas;
}
