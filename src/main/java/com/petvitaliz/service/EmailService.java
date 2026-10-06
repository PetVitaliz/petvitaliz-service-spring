package com.petvitaliz.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviarCodigoReset(String nome, String sobrenome, String destinatario, String codigo) {
        String html = """
                Olá <strong>%s %s</strong>,<br><br>
                Você solicitou uma redefinição de senha.<br><br>
                Copie o código abaixo e cole no site:<br><br>
                <h3>%s</h3><br><br>
                Este código expira em 10 minutos.<br><br>
                <strong>Caso essa solicitação não seja sua, ignore este e-mail.<br><br>
                Atenciosamente,<br>Equipe de Suporte PetVitaliz</strong>
                """.formatted(HtmlUtils.htmlEscape(nome), HtmlUtils.htmlEscape(sobrenome), codigo);

        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, "UTF-8");
            helper.setFrom(remetente, "Suporte Petvitaliz");
            helper.setTo(destinatario);
            helper.setSubject("Reset de Senha - PetVitaliz");
            helper.setText(html, true);
            mailSender.send(mensagem);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Falha ao montar o e-mail", e);
        }
    }
}
