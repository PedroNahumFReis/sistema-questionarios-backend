package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Pergunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String enunciado;
    private Double pontuacao;

    @ManyToOne
    @JoinColumn(name = "questionario_id")
    private Questionario questionario;

    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL)
    private List<Alternativa> alternativas; // RN05: Validar na criação se tem correta
}
