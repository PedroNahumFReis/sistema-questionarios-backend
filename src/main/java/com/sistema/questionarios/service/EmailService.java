package com.sistema.questionarios.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Serviço de envio de e-mail (RF09).
 *
 * Monta e envia ao aluno a mensagem com o link + token de acesso ao questionário.
 * @Service marca a classe como um componente de regra de negócio, gerenciado pelo Spring.
 */
@Service
public class EmailService {

    // Logger: permite registrar mensagens no console (info/aviso) em vez de usar System.out.
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    // Componente do Spring que efetivamente envia e-mails via SMTP (configurado no application.properties).
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailComToken(String destinatario, String token, Integer validadeMinutos) {
        // Link que o aluno clicará para responder. Em produção, a base da URL viria de configuração.
        String link = "http://localhost:8080/api/responder/" + token;

        // Monta a mensagem simples (destinatário, assunto e corpo em texto).
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatario);
        message.setSubject("Seu link de acesso ao Questionário");
        message.setText("Olá!\n\nVocê foi convidado a responder um questionário.\n" +
                "Acesse o link abaixo:\n" + link + "\n\n" +
                "Atenção: Este link expira em " + validadeMinutos + " minutos.\n\nBoa sorte!");

        try {
            mailSender.send(message);
            log.info("E-mail com token enviado para {}", destinatario);
        } catch (Exception ex) {
            // Importante: se o SMTP estiver indisponível (é fictício neste projeto), o envio falha.
            // Capturamos a exceção para NÃO derrubar a geração do token (RF08): o cadastro do token
            // continua válido e o link fica registrado no log. Em produção, configurar um SMTP real.
            log.warn("Falha ao enviar e-mail para {} (SMTP indisponível). Link: {} | Motivo: {}",
                    destinatario, link, ex.getMessage());
        }
    }
}
