package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Alternativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String texto;
    private Boolean correta;

    @ManyToOne
    @JoinColumn(name = "pergunta_id")
    private Pergunta pergunta;
}
