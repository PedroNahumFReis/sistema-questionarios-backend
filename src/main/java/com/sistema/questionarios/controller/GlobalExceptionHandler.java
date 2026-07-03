package com.sistema.questionarios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Tratador global de exceções.
 *
 * @RestControllerAdvice: intercepta exceções lançadas por QUALQUER controller e permite
 * transformá-las em respostas HTTP adequadas, em um único lugar (sem repetir try/catch).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converte violações de regra de negócio (ex.: RN05) em HTTP 400 (Bad Request), com uma
     * mensagem clara em JSON, em vez do genérico 500. O @ExceptionHandler diz qual exceção captura.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }
}
