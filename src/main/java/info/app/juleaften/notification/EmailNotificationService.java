package info.app.juleaften.notification;

import info.app.juleaften.config.EmailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service("emailNotificationService")
public class EmailNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;


    public EmailNotificationService(JavaMailSender mailSender, EmailConfig emailConfig) {
        this.mailSender = mailSender;
        this.emailConfig = emailConfig;
    }

    @Async
    public void sendMessage(String to, String subject, String content, boolean isHtml) {
        MimeMessage mimeMailMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMailMessage, true, StandardCharsets.UTF_8.name());

            messageHelper.setFrom(emailConfig.getFrom());
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, isHtml);

            mailSender.send(mimeMailMessage);
        } catch (MessagingException e) {
            LOGGER.error("Error while sending email to {}", to, e);
        }
    }
}
