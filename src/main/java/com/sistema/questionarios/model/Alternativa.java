package com.sistema.questionarios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade Alternativa (RF06).
 *
 * Uma opção de resposta de uma pergunta. O campo 'correta' indica se é a resposta certa.
 */
@Data
@Entity
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Texto exibido ao aluno (ex.: "4").
    private String texto;

    // true = esta é uma alternativa correta; usada na correção automática (RF12) e na regra RN05.
    private Boolean correta;

    // Pergunta à qual a alternativa pertence (coluna FK pergunta_id).
    // @JsonIgnore evita a recursão infinita Pergunta <-> Alternativa na serialização JSON.
    @ManyToOne
    @JoinColumn(name = "pergunta_id")
    @JsonIgnore
    private Pergunta pergunta;
}
