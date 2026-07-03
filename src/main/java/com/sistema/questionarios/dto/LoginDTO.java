package com.sistema.questionarios.dto;

import lombok.Data;

/**
 * DTO de login (RF03).
 *
 * DTO (Data Transfer Object) é um objeto simples usado só para transportar dados entre
 * o cliente e a API — aqui, o e-mail e a senha que o professor envia para entrar.
 * Usar um DTO evita expor a entidade Professor diretamente na entrada do endpoint.
 */
@Data
public class LoginDTO {
    private String email;
    private String senha;
}
