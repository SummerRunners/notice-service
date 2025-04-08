package xyz.srunners.notice.email.service;

import xyz.srunners.notice.email.dto.EmailDto.EmailRequestDto;

public interface MailService {

    void sendSimpleMail(EmailRequestDto requestDto);
    void sendHtmlMail(EmailRequestDto requestDto);
    void sendBulkMail(EmailRequestDto requestDto);
    void sendHtmlBulkMail(EmailRequestDto requestDto);

}