package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String matricula;
}
