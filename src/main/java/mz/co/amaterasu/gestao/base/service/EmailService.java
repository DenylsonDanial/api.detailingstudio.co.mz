package mz.co.amaterasu.gestao.base.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    /**
     * Envia um email HTML a partir de um template Thymeleaf em
     * {@code src/main/resources/templates/mail}.
     *
     * Falhas de envio sao registadas mas nao propagadas: o utilizador nao deve
     * ficar bloqueado por causa de uma indisponibilidade do servidor de email.
     */
    public void send(String to, String subject, String template, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String body = templateEngine.process("mail/" + template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email '{}' enviado para {}", subject, to);
        } catch (Exception e) {
            log.error("Falha ao enviar o email '{}' para {}: {}", subject, to, e.getMessage());
        }
    }
}
