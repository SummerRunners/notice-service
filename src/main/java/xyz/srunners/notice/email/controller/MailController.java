package xyz.srunners.notice.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.srunners.notice.email.dto.EmailDto;
import xyz.srunners.notice.email.service.MailService;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    // 단순 텍스트 메일 테스트
    @PostMapping("/send-mail")
    public ResponseEntity<String> sendMail(@Valid @RequestBody EmailDto.EmailRequestDto request) {
        mailService.sendSimpleMail(request);
        return ResponseEntity.ok("Successfully sent email to " + request.getTo());
    }

    // HTML 메일 테스트
    @PostMapping("/send-html-mail")
    public ResponseEntity<String> sendHtmlMail(@Valid @RequestBody EmailDto.EmailRequestDto request) {
        mailService.sendHtmlMail(request);
        return ResponseEntity.ok("Successfully sent HTML email to " + request.getTo());
    }

    // 메일 벌크 전송
    @PostMapping("/send-bulk-mail")
    public ResponseEntity<String> sendBulkMail(@Valid @RequestBody EmailDto.EmailRequestDto request) {
        mailService.sendBulkMail(request);
        return ResponseEntity.ok("Successfully sent bulk email.");
    }

    // HTML 메일 벌크 전송
    @PostMapping("/send-html-bulk-mail")
    public ResponseEntity<String> sendHtmlBulkMail(@Valid @RequestBody EmailDto.EmailRequestDto request) {
        mailService.sendHtmlBulkMail(request);
        return ResponseEntity.ok("Successfully sent bulk HTML email.");
    }


}