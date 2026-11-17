package com.petvitaliz.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Locale;

@Service
public class EmailService {

    private static final Locale BRAZIL = Locale.of("pt", "BR");

    private final JavaMailSender mailSender;
    private final String senderAddress;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String senderAddress) {
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
    }

    public void sendPasswordResetCode(String fullName, String recipient, String code) {
        String html = """
                Olá <strong>%s</strong>,<br><br>
                Você solicitou uma redefinição de senha.<br><br>
                Copie o código abaixo e cole no site:<br><br>
                <h3>%s</h3><br><br>
                Este código expira em 10 minutos.<br><br>
                <strong>Caso essa solicitação não seja sua, ignore este e-mail.<br><br>
                Atenciosamente,<br>Equipe de Suporte PetVitaliz</strong>
                """.formatted(HtmlUtils.htmlEscape(fullName), code);

        send("Suporte Petvitaliz", recipient, "Reset de Senha - PetVitaliz", html, null);
    }

    public void sendContact(String fullName, String accountEmail, String replyEmail, String message) {
        String escapedName = HtmlUtils.htmlEscape(fullName);

        String confirmation = """
                Olá <strong>%s</strong>,<br><br>
                Obrigado por entrar em contato! Sua mensagem foi recebida com sucesso.<br>
                Nossa equipe responderá em breve. 🐾<br><br>
                <strong>Atenciosamente,<br>Equipe PetVitaliz</strong>
                """.formatted(escapedName);

        String notice = """
                <h2>Novo contato recebido:</h2>
                <p><strong>Nome:</strong> %s</p>
                <p><strong>E-mail para resposta:</strong> %s</p>
                <p><strong>Mensagem:</strong><br>%s</p>
                """.formatted(
                escapedName,
                HtmlUtils.htmlEscape(replyEmail),
                HtmlUtils.htmlEscape(message).replace("\n", "<br>"));

        send("Equipe Petvitaliz", accountEmail, "Recebemos sua mensagem - PetVitaliz", confirmation, null);
        send("Contato Petvitaliz", senderAddress, "Novo Contato: " + fullName, notice, replyEmail);
    }

    public void sendSubscriptionConfirmed(String fullName, String recipient, String planName, BigDecimal price) {
        String html = """
                <h2>Olá, <strong>%s</strong>!</h2>
                <p>Estamos muito felizes em informar que a contratação do seu plano foi processada com sucesso.</p>
                <hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>
                <p><strong>Detalhes da Assinatura:</strong></p>
                <ul>
                    <li><strong>Plano:</strong> %s</li>
                    <li><strong>Valor:</strong> R$ %s / mês</li>
                    <li><strong>Status do Pagamento:</strong> Confirmado (Cartão de Crédito)</li>
                </ul>
                <hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>
                <p>A partir de agora, os benefícios já estão vinculados à sua conta. Se precisar de qualquer ajuda ou quiser agendar procedimentos, acesse seu painel.</p>
                <br>
                <p><strong>Atenciosamente,</strong><br>Equipe Financeira PetVitaliz</p>
                """.formatted(
                HtmlUtils.htmlEscape(fullName),
                HtmlUtils.htmlEscape(planName),
                String.format(BRAZIL, "%,.2f", price));

        send("Financeiro Petvitaliz", recipient, "Sua assinatura foi confirmada!", html, null);
    }

    private void send(String senderName, String recipient, String subject, String html, String replyTo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(senderAddress, senderName);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(html, true);
            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to build e-mail", e);
        }
    }
}
