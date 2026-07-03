package com.sistema.questionarios.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailComToken(String destinatario, String token, Integer validadeMinutos) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatario);
        message.setSubject("Seu link de acesso ao Questionário");

        // Em um cenário real, a base da URL estaria no application.properties
        String link = "http://localhost:8080/api/responder/" + token;

        message.setText("Olá!\n\nVocê foi convidado a responder um questionário.\n" +
                "Acesse o link abaixo:\n" + link + "\n\n" +
                "Atenção: Este link expira em " + validadeMinutos + " minutos.\n\nBoa sorte!");

        mailSender.send(message);
    }
}