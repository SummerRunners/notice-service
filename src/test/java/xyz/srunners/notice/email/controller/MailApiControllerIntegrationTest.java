package xyz.srunners.notice.email.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.srunners.notice.email.dto.EmailDto.SingleEmailRequest;
import xyz.srunners.notice.email.dto.EmailDto.BulkEmailRequest;

@SpringBootTest
@AutoConfigureMockMvc
class MailApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "lyaesley@gmail.com";
    
    @Test
    void testSendSingleMail() throws Exception {
        SingleEmailRequest request = SingleEmailRequest.builder()
            .to(TEST_EMAIL)
            .subject("단일 메일 테스트")
            .content("안녕하세요, 이것은 테스트 이메일입니다.")
            .build();

        mockMvc.perform(post("/api/mail/single")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void testSendSingleHtmlMail() throws Exception {
        SingleEmailRequest request = SingleEmailRequest.builder()
            .to(TEST_EMAIL)
            .subject("HTML 메일 테스트")
            .content("""
                <h1>HTML 테스트 이메일</h1>
                <p>이것은 <strong>HTML</strong> 형식의 테스트 이메일입니다.</p>
                <ul>
                    <li>항목 1</li>
                    <li>항목 2</li>
                </ul>
                """)
            .build();

        mockMvc.perform(post("/api/mail/single/html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void testSendBulkMail() throws Exception {
        BulkEmailRequest request = BulkEmailRequest.builder()
            .recipients(List.of(
                TEST_EMAIL,
                "another-test-email@example.com" // 두 번째 테스트 이메일 주소
            ))
            .subject("대량 메일 테스트")
            .content("이것은 대량 발송 테스트 이메일입니다.")
            .build();

        mockMvc.perform(post("/api/mail/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());
    }

    @Test
    void testSendBulkHtmlMail() throws Exception {
        BulkEmailRequest request = BulkEmailRequest.builder()
            .recipients(List.of(
                TEST_EMAIL,
                "yulwhite1@naver.com" // 두 번째 테스트 이메일 주소
            ))
            .subject("대량 HTML 메일 테스트")
            .content("""
                <h1>대량 HTML 테스트</h1>
                <p>이것은 <strong>대량 발송</strong> HTML 테스트 이메일입니다.</p>
                <div style="color: blue;">
                    테스트 메시지입니다.
                </div>
                """)
            .build();

        mockMvc.perform(post("/api/mail/bulk/html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());
    }
}