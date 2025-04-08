package xyz.srunners.notice.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.srunners.notice.email.dto.EmailDto.EmailRequestDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService{

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // 일반 텍스트 메일 전송
    @Override
    public void sendSimpleMail(EmailRequestDto request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getContent(), false); // 일반 텍스트 전송
            mailSender.send(message);
            log.info("Successfully sent email to {}", request.getTo());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // HTML 형식 메일 전송
    @Override
    public void sendHtmlMail(EmailRequestDto request) {
        try {
            // Thymeleaf 템플릿 처리
            String htmlContent = createHtmlContent("welcome_email", request);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(htmlContent, true); // HTML 전송
            mailSender.send(message);
            log.info("Successfully sent HTML email to {}", request.getTo());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendBulkMail(EmailRequestDto request) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (String to : request.getToList()) {
            executorService.submit(() -> {
                try {
                    sendSimpleMail(EmailRequestDto.builder()
                        .to(to)
                        .subject(request.getSubject())
                        .content(request.getContent())
                        .build());
                } catch (Exception e) {
                    log.error("Error sending email to {}: {}", to, e.getMessage());
                }
            });
        }
        executorWating(executorService);

    }

    @Override
    public void sendHtmlBulkMail(EmailRequestDto request) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (String to : request.getToList()) {
            executorService.submit(() -> {
                try {
                    sendHtmlMail(EmailRequestDto.builder()
                        .to(to)
                        .subject(request.getSubject())
                        .content(request.getContent())
                        .build());
                } catch (Exception e) {
                    log.error("Error sending HTML email to {}: {}", to, e.getMessage());
                }
            });
        }
        executorWating(executorService);

    }

    private void executorWating(ExecutorService executorService) {
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                log.error("Timeout occurred while waiting for email tasks to complete.");
            }
        } catch (InterruptedException e) {
            log.error("ExecutorService interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private String createHtmlContent(String templateName, EmailRequestDto request) {

        Context context = new Context();
        context.setVariables(EmailRequestDto.convertToMap(request));
        return templateEngine.process(templateName, context);
    }

}