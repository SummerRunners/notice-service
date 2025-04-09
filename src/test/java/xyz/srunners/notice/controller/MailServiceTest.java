package xyz.srunners.notice.controller;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.srunners.notice.email.dto.EmailDto.EmailRequestDto;
import xyz.srunners.notice.email.service.MailService;

@SpringBootTest
class MailServiceTest {

    @Autowired
    private MailService mailService;

    private static List<String> emailList;

    @BeforeAll
    static void beforeAll() {
//        emailList = Arrays.asList("lyaesley@gmail.com", "lgm3555@gmail.com", "eo72728836@gmail.com", "dmsk.ess@gmail.com", "ghdrms1992biz@gmail.com");
        emailList = List.of("lyaesley@gmail.com");
    }

    @Test
    void testHtmlBulkMailSend() {

        EmailRequestDto requestDto = EmailRequestDto.builder()
                                                    .subject("당신을 초대합니다.")
                                                    .content("""
                                                        <h1>같이하자</h1>
                                                        <p><b>스들바를 위하여</b></p>
                                                        """)
                                                    .toList(emailList)
                                                    .build();

        // 대량 HTML 메일 발송
        mailService.sendHtmlBulkMail(requestDto);

        // 확인 메시지 출력
        System.out.println("Bulk HTML email process started...");
    }

    @Test
    @DisplayName("html 템플릿 단건 전송")
    void testSendHtmlMail() {

        EmailRequestDto requestDto = EmailRequestDto.builder()
                                                    .subject("당신을 초대합니다.")
                                                    .content("""
                                                        <h1>같이하자</h1>
                                                        <p><b>스들바를 위하여</b></p>
                                                        """)
                                                    .to("lyaesley@gmail.com")
                                                    .build();

        mailService.sendHtmlMail(requestDto);
    }

}