package com.sistema.questionarios.dto;

import lombok.Data;

import java.util.List;

@Data
public class RespostaAlunoDTO {
    private List<Long> idsAlternativasSelecionadas;
}
