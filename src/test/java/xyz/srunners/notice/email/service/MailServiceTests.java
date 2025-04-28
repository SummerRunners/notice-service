package xyz.srunners.notice.email.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import xyz.srunners.notice.email.dto.EmailDto.BulkEmailRequest;
import xyz.srunners.notice.email.dto.EmailDto.SingleEmailRequest;

@SpringBootTest
class MailServiceTest {
    @MockBean
    private JavaMailSender mailSender;
    
    @MockBean
    private EmailTemplateService emailTemplateService;

    @Autowired
    private MailService mailService;

    @Test
    void testSendSimpleMail() {
        // Given
        SingleEmailRequest request = SingleEmailRequest.builder()
                                                       .to("test@example.com")
                                                       .subject("테스트 제목")
                                                       .content("테스트 내용")
                                                       .build();

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        // When & Then
        assertDoesNotThrow(() -> mailService.sendMail(request));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(emailTemplateService, never()).generateTemplate(anyString(), anyMap());
    }

    @Test
    void testSendHtmlMail() {
        // Given
        SingleEmailRequest request = SingleEmailRequest.builder()
            .to("test@example.com")
            .subject("HTML 테스트 제목")
            .content("<h1>테스트 내용</h1>")
            .build();

        String processedTemplate = "<html><body><h1>테스트 내용</h1></body></html>";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailTemplateService.generateTemplate(eq("mail/welcome"), any()))
            .thenReturn(processedTemplate);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        // When & Then
        assertDoesNotThrow(() -> mailService.sendHtmlMail(request));
        
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(emailTemplateService, times(1)).generateTemplate(eq("mail/welcome"), any());
    }

    @Test
    void testSendBulkHtmlMail() {
        // Given
        List<String> recipients = Arrays.asList("lyaesley@gmail.com", "yulwhite1@naver.com");
        BulkEmailRequest request = BulkEmailRequest.builder()
                                                   .recipients(recipients)
                                                   .subject("벌크 테스트")
                                                   .content("<h1>벌크 테스트 내용</h1>")
                                                   .build();

        String processedTemplate = "<html><body><h1>벌크 테스트 내용</h1></body></html>";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailTemplateService.generateTemplate(eq("mail/welcome"), any()))
            .thenReturn(processedTemplate);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        mailService.sendHtmlBulkMail(request);

        // Then - 비동기 처리 대기
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mailSender, times(recipients.size())).send(any(MimeMessage.class));
            verify(emailTemplateService, times(recipients.size()))
                .generateTemplate(eq("mail/welcome"), any());
        });
    }
}