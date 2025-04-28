package xyz.srunners.notice.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.srunners.notice.annotation.LogMailSend;
import xyz.srunners.notice.email.dto.EmailDto.BulkEmailRequest;
import xyz.srunners.notice.email.dto.EmailDto.SingleEmailRequest;
import xyz.srunners.notice.email.exception.EmailException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;
    private final ApplicationContext applicationContext;

    @Override
    @LogMailSend(isHtml = false)
    public void sendMail(SingleEmailRequest request) {
        try {
            MimeMessage message = createMimeMessage(request, false);
            mailSender.send(message);
            log.debug("이메일 전송 성공: {}", request.getTo());
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new EmailException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @LogMailSend(isHtml = true)
    public void sendHtmlMail(SingleEmailRequest request) {
        try {
            MimeMessage message = createMimeMessage(request, true);
            mailSender.send(message);
            log.debug("HTML 이메일 전송 성공: {}", request.getTo());
        } catch (Exception e) {
            log.error("HTML 이메일 전송 실패: {}", e.getMessage());
            throw new EmailException("HTML 이메일 전송 중 오류가 발생했습니다.", e);
        }
    }

    @Async
    @Override
    @LogMailSend(isHtml = false, isBulk = true)
    public CompletableFuture<Void> sendBulkMail(BulkEmailRequest request) {
        return CompletableFuture.runAsync(() -> {
            // 프록시된 자기 자신 가져오기
            MailService self = applicationContext.getBean(MailService.class);
            log.info("대량 이메일 전송 시작: 수신자 {} 명", request.getRecipients().size());
            
            // 병렬 처리에서 발생하는 예외를 모두 수집
            request.getRecipients().forEach(recipient -> {
                try {
                    // 프록시를 통한 호출로 AOP 적용
                    self.sendMail(SingleEmailRequest.builder()
                                   .to(recipient)
                                   .subject(request.getSubject())
                                   .content(request.getContent())
                                   .build());
                } catch (Exception e) {
                    log.error("대량 이메일 전송 실패 (수신자: {}): {}", recipient, e.getMessage());
                    // 개별 메일 실패는 전체 프로세스를 중단하지 않음
                }
            });
            
            log.info("대량 이메일 전송 작업 완료");
        });
    }

    @Async
    @Override
    @LogMailSend(isHtml = true, isBulk = true)
    public CompletableFuture<Void> sendHtmlBulkMail(BulkEmailRequest request) {
        return CompletableFuture.runAsync(() -> {
            // 프록시된 자기 자신 가져오기
            MailService self = applicationContext.getBean(MailService.class);
            log.info("대량 HTML 이메일 전송 시작: 수신자 {} 명", request.getRecipients().size());
            
            request.getRecipients().forEach(recipient -> {
                try {
                    // 프록시를 통한 호출로 AOP 적용
                    self.sendHtmlMail(SingleEmailRequest.builder()
                                   .to(recipient)
                                   .subject(request.getSubject())
                                   .content(request.getContent())
                                   .build());
                } catch (Exception e) {
                    log.error("대량 HTML 이메일 전송 실패 (수신자: {}): {}", recipient, e.getMessage());
                    // 개별 메일 실패는 전체 프로세스를 중단하지 않음
                }
            });
            
            log.info("대량 HTML 이메일 전송 작업 완료");
        });
    }

    private MimeMessage createMimeMessage(SingleEmailRequest request, boolean isHtml) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());

        String content = isHtml ?
            emailTemplateService.generateTemplate("mail/welcome", request.toTemplateVariables()) :
            request.getContent();

        helper.setText(content, isHtml);
        return message;
    }
}