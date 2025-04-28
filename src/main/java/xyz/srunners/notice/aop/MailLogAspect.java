package xyz.srunners.notice.aop;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import xyz.srunners.notice.annotation.LogMailSend;
import xyz.srunners.notice.email.dto.EmailDto.BulkEmailRequest;
import xyz.srunners.notice.email.dto.EmailDto.SingleEmailRequest;
import xyz.srunners.notice.email.dto.MailLog;
import xyz.srunners.notice.email.dto.MailStatus;
import xyz.srunners.notice.email.repository.MailLogRepository;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MailLogAspect {
    private final MailLogRepository mailLogRepository;
    
    // 벌크 작업 정보 저장 맵 - 전역 상태로 관리
    private static final Map<String, BulkMailJobInfo> bulkJobInfoMap = new ConcurrentHashMap<>();
    
    // 각 이메일 주소에 대응하는 벌크 작업 ID
    private static final Map<String, String> recipientToBulkJobIdMap = new ConcurrentHashMap<>();
    
    @Around("@annotation(logMailSend)")
    public Object logMailExecution(ProceedingJoinPoint joinPoint, LogMailSend logMailSend) throws Throwable {
        if (logMailSend.isBulk()) {
            return handleBulkMail(joinPoint, logMailSend);
        } else {
            return handleSingleMail(joinPoint, logMailSend);
        }
    }
    
    // 단일 이메일 처리 (벌크의 일부일 수도 있음)
    private Object handleSingleMail(ProceedingJoinPoint joinPoint, LogMailSend logMailSend) throws Throwable {
        Object[] args = joinPoint.getArgs();
        SingleEmailRequest request = extractEmailRequest(args);
        String recipientEmail = request.getTo();
        
        // 이 이메일이 벌크 작업의 일부인지 확인
        String bulkJobId = recipientToBulkJobIdMap.get(recipientEmail);
        if (bulkJobId != null) {
            log.debug("수신자 [{}]는 벌크 작업 ID [{}]의 일부입니다", recipientEmail, bulkJobId);
            return logMailWithBulkContext(joinPoint, request, bulkJobId, logMailSend);
        } else {
            return logStandaloneMail(joinPoint, request, logMailSend);
        }
    }
    
    // 독립적인 단일 이메일 로깅
    private Object logStandaloneMail(ProceedingJoinPoint joinPoint, SingleEmailRequest request, LogMailSend logMailSend) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MailLog mailLog = createMailLog(request, logMailSend, null);
        
        try {
            Object result = joinPoint.proceed();
            updateMailLogOnSuccess(mailLog);
            return result;
        } catch (Exception e) {
            updateMailLogOnFailure(mailLog, e);
            throw e;
        } finally {
            completeMailLog(mailLog, startTime);
            mailLogRepository.save(mailLog);
        }
    }
    
    // 벌크 컨텍스트를 가진 이메일 로깅
    private Object logMailWithBulkContext(ProceedingJoinPoint joinPoint, SingleEmailRequest request, 
                                        String bulkJobId, LogMailSend logMailSend) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MailLog mailLog = createMailLog(request, logMailSend, bulkJobId);
        mailLog.getMetadata().put("partOfBulkJob", true);
        
        try {
            Object result = joinPoint.proceed();
            updateMailLogOnSuccess(mailLog);
            return result;
        } catch (Exception e) {
            updateMailLogOnFailure(mailLog, e);
            throw e;
        } finally {
            completeMailLog(mailLog, startTime);
            mailLogRepository.save(mailLog);
            
            // 벌크 작업 진행 상황 업데이트
            updateBulkJobProgress(bulkJobId, mailLog.getStatus() == MailStatus.SUCCESS);
        }
    }
    
    // 벌크 이메일 처리
    private Object handleBulkMail(ProceedingJoinPoint joinPoint, LogMailSend logMailSend) throws Throwable {
        Object[] args = joinPoint.getArgs();
        BulkEmailRequest request = extractBulkEmailRequest(args);
        
        // 새로운 벌크 작업 ID 생성 및 정보 초기화
        String bulkJobId = initBulkJob(request, logMailSend);
        
        try {
            // 각 수신자를 벌크 작업 ID와 매핑
            for (String recipient : request.getRecipients()) {
                recipientToBulkJobIdMap.put(recipient, bulkJobId);
                log.debug("수신자 [{}]를 벌크 작업 ID [{}]에 매핑", recipient, bulkJobId);
            }
            
            // 대량 메일 전송 메서드 실행
            CompletableFuture<Void> future = (CompletableFuture<Void>) joinPoint.proceed();
            
            // 완료 콜백 추가
            return future.whenComplete((result, ex) -> {
                try {
                    completeBulkJob(bulkJobId, ex);
                } finally {
                    // 수신자 매핑 정리
                    for (String recipient : request.getRecipients()) {
                        recipientToBulkJobIdMap.remove(recipient);
                    }
                }
            });
        } catch (Exception e) {
            // 즉시 발생하는 예외 처리
            failBulkJob(bulkJobId, e);
            
            // 수신자 매핑 정리
            for (String recipient : request.getRecipients()) {
                recipientToBulkJobIdMap.remove(recipient);
            }
            
            throw e;
        }
    }
    
    // 벌크 작업 초기화 및 요약 로그 생성
    private String initBulkJob(BulkEmailRequest request, LogMailSend logMailSend) {
        String bulkJobId = UUID.randomUUID().toString();
        
        // 벌크 작업 요약 로그 생성
        MailLog summaryLog = MailLog.builder()
                                .jobId(bulkJobId)
                                .subject(request.getSubject())
                                .content(request.getContent())
                                .isHtml(logMailSend.isHtml())
                                .isBulk(true)
                                .status(MailStatus.REQUESTED)
                                .requestedAt(LocalDateTime.now())
                                .metadata(new HashMap<>())
                                .build();
        
        int recipientCount = request.getRecipients().size();
        summaryLog.getMetadata().put("recipientCount", recipientCount);
        summaryLog.getMetadata().put("recipients", formatRecipientsList(request.getRecipients()));
        
        // 요약 로그 저장
        mailLogRepository.save(summaryLog);
        
        // 작업 진행 정보 초기화
        BulkMailJobInfo jobInfo = BulkMailJobInfo.builder()
                                                 .summaryLog(summaryLog)
                                                 .totalCount(recipientCount)
                                                 .startTime(System.currentTimeMillis())
                                                 .build();
        bulkJobInfoMap.put(bulkJobId, jobInfo);
        
        log.info("벌크 이메일 작업 [{}] 시작, 총 수신자: {}", bulkJobId, recipientCount);
        return bulkJobId;
    }
    
    // 벌크 작업 완료 처리
    private void completeBulkJob(String bulkJobId, Throwable ex) {
        BulkMailJobInfo jobInfo = bulkJobInfoMap.remove(bulkJobId);
        if (jobInfo == null) {
            log.warn("벌크 작업 ID [{}]에 대한 정보를 찾을 수 없습니다", bulkJobId);
            return;
        }
        
        MailLog summaryLog = jobInfo.getSummaryLog();
        summaryLog.setStatus(ex == null ? MailStatus.SUCCESS : MailStatus.FAILED);
        summaryLog.setCompletedAt(LocalDateTime.now());
        
        long executionTime = System.currentTimeMillis() - jobInfo.getStartTime();
        summaryLog.getMetadata().put("executionTimeMs", executionTime);
        summaryLog.getMetadata().put("successCount", jobInfo.getSuccessCount());
        summaryLog.getMetadata().put("failCount", jobInfo.getFailCount());
        
        if (ex != null) {
            summaryLog.setErrorMessage(ex.getMessage());
        }
        
        mailLogRepository.save(summaryLog);
        log.info("벌크 이메일 작업 [{}] 완료, 성공: {}, 실패: {}, 소요시간: {}ms", 
                bulkJobId, jobInfo.getSuccessCount(), jobInfo.getFailCount(), executionTime);
    }
    
    // 벌크 작업 실패 처리
    private void failBulkJob(String bulkJobId, Exception e) {
        BulkMailJobInfo jobInfo = bulkJobInfoMap.remove(bulkJobId);
        if (jobInfo == null) {
            log.warn("벌크 작업 ID [{}]에 대한 정보를 찾을 수 없습니다", bulkJobId);
            return;
        }
        
        MailLog summaryLog = jobInfo.getSummaryLog();
        summaryLog.setStatus(MailStatus.FAILED);
        summaryLog.setErrorMessage(e.getMessage());
        summaryLog.setCompletedAt(LocalDateTime.now());
        
        long executionTime = System.currentTimeMillis() - jobInfo.getStartTime();
        summaryLog.getMetadata().put("executionTimeMs", executionTime);
        summaryLog.getMetadata().put("successCount", jobInfo.getSuccessCount());
        summaryLog.getMetadata().put("failCount", jobInfo.getFailCount());
        
        mailLogRepository.save(summaryLog);
        log.error("벌크 이메일 작업 [{}] 초기화 실패: {}", bulkJobId, e.getMessage());
    }
    
    // 벌크 작업 진행 상황 업데이트
    private void updateBulkJobProgress(String bulkJobId, boolean success) {
        BulkMailJobInfo jobInfo = bulkJobInfoMap.get(bulkJobId);
        if (jobInfo != null) {
            if (success) {
                jobInfo.incrementSuccessCount();
            } else {
                jobInfo.incrementFailCount();
            }
        }
    }
    
    // 공통 메서드: 메일 로그 생성
    private MailLog createMailLog(SingleEmailRequest request, LogMailSend logMailSend, String bulkJobId) {
        return MailLog.builder()
                .jobId(bulkJobId)
                .recipient(request.getTo())
                .subject(request.getSubject())
                .content(request.getContent())
                .isHtml(logMailSend.isHtml())
                .isBulk(false)
                .status(MailStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .metadata(new HashMap<>())
                .build();
    }
    
    // 공통 메서드: 성공 상태 업데이트
    private void updateMailLogOnSuccess(MailLog mailLog) {
        mailLog.setStatus(MailStatus.SUCCESS);
    }
    
    // 공통 메서드: 실패 상태 업데이트
    private void updateMailLogOnFailure(MailLog mailLog, Exception e) {
        mailLog.setStatus(MailStatus.FAILED);
        mailLog.setErrorMessage(e.getMessage());
    }
    
    // 공통 메서드: 로그 완료
    private void completeMailLog(MailLog mailLog, long startTime) {
        mailLog.setCompletedAt(LocalDateTime.now());
        mailLog.getMetadata().put("executionTimeMs", System.currentTimeMillis() - startTime);
    }
    
    // 공통 메서드: 수신자 목록 포맷팅
    private String formatRecipientsList(List<String> recipients) {
        if (recipients.size() <= 10) {
            return String.join(",", recipients);
        } else {
            List<String> firstTen = recipients.subList(0, 10);
            return String.join(",", firstTen) + "...";
        }
    }
    
    // 메서드 인자에서 EmailRequest 객체 추출 메서드들
    private SingleEmailRequest extractEmailRequest(Object[] args) {
        return Arrays.stream(args)
                    .filter(arg -> arg instanceof SingleEmailRequest)
                    .map(arg -> (SingleEmailRequest)arg)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("SingleEmailRequest not found"));
    }

    private BulkEmailRequest extractBulkEmailRequest(Object[] args) {
        return Arrays.stream(args)
                     .filter(arg -> arg instanceof BulkEmailRequest)
                     .map(arg -> (BulkEmailRequest)arg)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("BulkEmailRequest not found"));
    }
    
    // 벌크 작업 정보를 저장하는 내부 클래스
    @Data
    @Builder
    @AllArgsConstructor
    private static class BulkMailJobInfo {
        private final MailLog summaryLog;
        private final int totalCount;
        private final long startTime;
        @Builder.Default
        private int successCount = 0;
        @Builder.Default
        private int failCount = 0;
        
        public void incrementSuccessCount() {
            successCount++;
        }
        
        public void incrementFailCount() {
            failCount++;
        }
    }
}