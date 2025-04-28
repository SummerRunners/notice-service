package xyz.srunners.notice.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.srunners.notice.email.dto.EmailDto.SingleEmailRequest;
import xyz.srunners.notice.email.dto.EmailDto.BulkEmailRequest;
import xyz.srunners.notice.email.dto.ErrorResponse;
import xyz.srunners.notice.email.exception.EmailException;
import xyz.srunners.notice.email.service.MailService;

@Slf4j
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailApiController {
    private final MailService mailService;

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(EmailException e) {
        log.error("이메일 처리 중 오류: ", e);
        return ResponseEntity.internalServerError()
                             .body(new ErrorResponse("이메일 전송 실패", e.getMessage()));
    }

    @PostMapping("/single")
    public ResponseEntity<String> sendMail(@Valid @RequestBody SingleEmailRequest request) {
        log.info("일반 이메일 전송 요청: 수신자={}, 제목={}", request.getTo(), request.getSubject());
        mailService.sendMail(request);
        return ResponseEntity.ok("이메일이 " + request.getTo() + "에게 성공적으로 전송되었습니다.");
    }
    
    @PostMapping("/single/html")
    public ResponseEntity<String> sendHtmlMail(@Valid @RequestBody SingleEmailRequest request) {
        log.info("HTML 이메일 전송 요청: 수신자={}, 제목={}", request.getTo(), request.getSubject());
        mailService.sendHtmlMail(request);
        return ResponseEntity.ok("HTML 이메일이 " + request.getTo() + "에게 성공적으로 전송되었습니다.");
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<String> sendBulkMail(@Valid @RequestBody BulkEmailRequest request) {
        int recipientCount = request.getRecipients().size();
        log.info("대량 이메일 전송 요청: 수신자 수={}, 제목={}", recipientCount, request.getSubject());
        mailService.sendBulkMail(request);
        return ResponseEntity.accepted().body(recipientCount + "명의 수신자에게 대량 이메일 전송이 시작되었습니다.");
    }
    
    @PostMapping("/bulk/html")
    public ResponseEntity<String> sendHtmlBulkMail(@Valid @RequestBody BulkEmailRequest request) {
        int recipientCount = request.getRecipients().size();
        log.info("대량 HTML 이메일 전송 요청: 수신자 수={}, 제목={}", recipientCount, request.getSubject());
        mailService.sendHtmlBulkMail(request);
        return ResponseEntity.accepted().body(recipientCount + "명의 수신자에게 대량 HTML 이메일 전송이 시작되었습니다.");
    }
}