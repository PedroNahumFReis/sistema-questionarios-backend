package com.sistema.questionarios.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidade Professor (RF01).
 *
 * @Entity marca esta classe como uma tabela do banco (uma linha = um professor).
 * @Data (Lombok) gera automaticamente getters, setters, toString, equals e hashCode,
 * evitando escrever esse código repetitivo à mão.
 */
@Data
@Entity
public class Professor {

    // Chave primária. @GeneratedValue(IDENTITY) faz o banco gerar o id automaticamente (auto_increment).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // @Column(unique = true): o banco impede dois professores com o mesmo e-mail.
    @Column(unique = true)
    private String email;

    // Guardada de forma criptografada (BCrypt) pelo ProfessorController antes de salvar. Nunca em texto puro.
    private String senha;
}
