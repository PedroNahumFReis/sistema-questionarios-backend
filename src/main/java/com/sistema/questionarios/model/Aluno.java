package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade Aluno (RF02).
 *
 * Representa um aluno previamente cadastrado que poderá receber questionários (RN02).
 */
@Data
@Entity
public class Aluno {

    // Chave primária gerada pelo banco.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // E-mail único: é para onde o link/token do questionário será enviado (RF09).
    @Column(unique = true)
    private String email;

    // Matrícula única do aluno.
    @Column(unique = true)
    private String matricula;
}
