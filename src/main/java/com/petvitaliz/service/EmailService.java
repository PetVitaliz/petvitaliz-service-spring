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

        enviarHtml("Suporte Petvitaliz", destinatario, "Reset de Senha - PetVitaliz", html, null);
    }
    public void enviarContato(String nome, String sobrenome, String emailConta,
                              String emailParaResposta, String mensagem) {
        String nomeCompleto = HtmlUtils.htmlEscape(nome + " " + sobrenome);

        String confirmacao = """
                Olá <strong>%s</strong>,<br><br>
                Obrigado por entrar em contato! Sua mensagem foi recebida com sucesso.<br>
                Nossa equipe responderá em breve. 🐾<br><br>
                <strong>Atenciosamente,<br>Equipe PetVitaliz</strong>
                """.formatted(nomeCompleto);

        String aviso = """
                <h2>Novo contato recebido:</h2>
                <p><strong>Nome:</strong> %s</p>
                <p><strong>E-mail para resposta:</strong> %s</p>
                <p><strong>Mensagem:</strong><br>%s</p>
                """.formatted(
                nomeCompleto,
                HtmlUtils.htmlEscape(emailParaResposta),
                HtmlUtils.htmlEscape(mensagem).replace("\n", "<br>"));

        enviarHtml("Equipe Petvitaliz", emailConta, "Recebemos sua mensagem - PetVitaliz", confirmacao, null);
        enviarHtml("Contato Petvitaliz", remetente, "Novo Contato: " + nome + " " + sobrenome, aviso, emailParaResposta);
    }

    public void enviarPlanoAssinado(String nome, String sobrenome, String destinatario,
                                    String nomePlano, BigDecimal preco) {
        String valor = String.format(Locale.of("pt", "BR"), "%,.2f", preco);

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
                """.formatted(HtmlUtils.htmlEscape(nome + " " + sobrenome), HtmlUtils.htmlEscape(nomePlano), valor);

        enviarHtml("Financeiro Petvitaliz", destinatario, "Sua assinatura foi confirmada!", html, null);
    }

    private void enviarHtml(String nomeRemetente, String para, String assunto, String html, String responderPara) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, "UTF-8");
            helper.setFrom(remetente, nomeRemetente);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(html, true);
            if (responderPara != null && !responderPara.isBlank()) {
                helper.setReplyTo(responderPara);
            }
            mailSender.send(mensagem);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Falha ao montar o e-mail", e);
        }
    }
}
