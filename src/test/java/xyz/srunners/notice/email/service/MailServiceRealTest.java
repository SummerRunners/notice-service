package xyz.srunners.notice.email.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import xyz.srunners.notice.email.dto.EmailDto.BulkEmailRequest;
import xyz.srunners.notice.email.dto.EmailDto.SingleEmailRequest;
import xyz.srunners.notice.email.dto.MailLog;
import xyz.srunners.notice.email.dto.MailStatus;
import xyz.srunners.notice.email.repository.MailLogRepository;

@SpringBootTest
@ActiveProfiles("local")
class MailServiceRealTest {

    @Autowired
    private MailService mailService;
    
    @Autowired
    private MailLogRepository mailLogRepository;
    
    private static final String TEST_EMAIL = "lyaesley@gmail.com";
    private static final String TEST_EMAIL2 = "yulwhite1@naver.com";

    @Test
    void testSendRealEmail() {
        // Given
        SingleEmailRequest request = SingleEmailRequest.builder()
                                                       .to(TEST_EMAIL)
                                                       .subject("실제 테스트 이메일")
                                                       .content("안녕하세요, 이것은 실제로 전송되는 테스트 이메일입니다.")
                                                       .build();

        // When
        mailService.sendMail(request);
        
        // Then
        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> {
                   Optional<MailLog> log = mailLogRepository.findByRecipient(TEST_EMAIL);
                   return log.isPresent() && log.get().getStatus() == MailStatus.SUCCESS;
               });
    }

    @Test
    void testSendRealBulkEmail() {
        // Given
        List<String> recipients = Arrays.asList(TEST_EMAIL, TEST_EMAIL2);
        
        BulkEmailRequest request = BulkEmailRequest.builder()
                                                  .recipients(recipients)
                                                  .subject("실제 대량 테스트 이메일")
                                                  .content("이것은 실제로 전송되는 대량 테스트 이메일입니다.")
                                                  .build();

        // When
        CompletableFuture<Void> future = mailService.sendBulkMail(request);

        // Then - 비동기 작업 완료 대기
        future.join(); // 작업이 완료될 때까지 대기
        
        await().atMost(3, TimeUnit.SECONDS)
               .until(() -> {
                   // 모든 개별 메일 로그 확인
                   boolean allIndividualLogsExist = recipients.stream()
                       .allMatch(email -> mailLogRepository.findFirstByRecipientOrderByRequestedAtDesc(email).isPresent());
                   
                   // 벌크 작업 요약 로그 확인
                   List<MailLog> bulkLogs = mailLogRepository.findAllByIsBulkTrue();
                   boolean bulkLogExists = bulkLogs.stream()
                       .anyMatch(log -> log.getStatus() == MailStatus.SUCCESS);
                   
                   return allIndividualLogsExist && bulkLogExists;
               });
    }
}