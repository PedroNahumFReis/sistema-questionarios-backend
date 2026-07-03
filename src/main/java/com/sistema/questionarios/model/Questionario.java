package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade Questionario (RF04).
 *
 * Um questionário pertence a um professor e possui uma lista de perguntas.
 */
@Data
@Entity
public class Questionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descricao;

    // Preenchida automaticamente no momento da criação do objeto.
    private LocalDateTime dataCriacao = LocalDateTime.now();

    // Quanto tempo (em minutos) o token de acesso enviado ao aluno fica válido (RNF05).
    private Integer tempoValidadeTokenMinutos;

    // Lado "muitos" da relação: vários questionários podem ser de um mesmo professor.
    // @JoinColumn indica a coluna de chave estrangeira (professor_id) nesta tabela.
    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    // Lado "um" da relação: um questionário tem várias perguntas.
    // mappedBy = "questionario": a relação é controlada pelo campo 'questionario' da classe Pergunta.
    // cascade = ALL: ao salvar/excluir o questionário, as perguntas são salvas/excluídas junto.
    @OneToMany(mappedBy = "questionario", cascade = CascadeType.ALL)
    private List<Pergunta> perguntas;
}
