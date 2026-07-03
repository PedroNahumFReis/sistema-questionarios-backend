package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Questionario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private Integer tempoValidadeTokenMinutos; // RNF05

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @OneToMany(mappedBy = "questionario", cascade = CascadeType.ALL)
    private List<Pergunta> perguntas;
}
