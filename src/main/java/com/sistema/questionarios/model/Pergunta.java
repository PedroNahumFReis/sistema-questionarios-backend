package com.sistema.questionarios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Entidade Pergunta (RF05).
 *
 * Cada pergunta pertence a um questionário e possui uma lista de alternativas.
 */
@Data
@Entity
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String enunciado;

    // Tipo da pergunta (RF05). @Enumerated(STRING) guarda o NOME do enum no banco
    // (ex.: "UNICA_ESCOLHA") em vez do número da posição — mais legível e seguro a mudanças.
    @Enumerated(EnumType.STRING)
    private TipoPergunta tipo;

    // Quantos pontos a pergunta vale (somados à nota quando o aluno acerta).
    private Double pontuacao;

    // Lado "muitos": várias perguntas pertencem a um questionário (coluna FK questionario_id).
    // @JsonIgnore: evita recursão infinita na serialização JSON. Sem isso, o JSON faria
    // Questionario -> perguntas -> Pergunta -> questionario -> perguntas... sem fim.
    @ManyToOne
    @JoinColumn(name = "questionario_id")
    @JsonIgnore
    private Questionario questionario;

    // Lado "um": uma pergunta tem várias alternativas. cascade = ALL salva/exclui as alternativas junto.
    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL)
    private List<Alternativa> alternativas;
}
