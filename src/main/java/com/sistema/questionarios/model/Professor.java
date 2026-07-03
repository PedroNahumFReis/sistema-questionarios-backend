package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @Column(unique = true)
    private String email;
    private String senha;
}
