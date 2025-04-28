package xyz.srunners.notice.email.service;

import xyz.srunners.notice.annotation.LogMailSend;
import xyz.srunners.notice.email.dto.EmailDto.SingleEmailRequest;
import xyz.srunners.notice.email.dto.EmailDto.BulkEmailRequest;
import java.util.concurrent.CompletableFuture;

public interface MailService {
    void sendMail(SingleEmailRequest request);

    void sendHtmlMail(SingleEmailRequest request);

    CompletableFuture<Void> sendBulkMail(BulkEmailRequest request);

    CompletableFuture<Void> sendHtmlBulkMail(BulkEmailRequest request);
}