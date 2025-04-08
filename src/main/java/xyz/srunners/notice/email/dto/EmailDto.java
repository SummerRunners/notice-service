package xyz.srunners.notice.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class EmailDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailRequestDto {
        // 단일 수신자 이메일
        @Email
        @NotBlank
        private String to;  // 받는 사람 이메일

        // 다수의 수신자 이메일 (Bulk 용도)
        private List<@Email @NotBlank String> toList; // 여러 수신자 이메일

        @NotBlank
        private String subject; // 이메일 제목

        @NotBlank
        private String content; // 이메일 내용

        public static Map<String, Object> convertToMap(EmailRequestDto request) {
            Map<String, Object> map = new HashMap<>();
            map.put("email", request.getTo());
            map.put("subject", request.getSubject());
            map.put("content", request.getContent());
            return map;
        }
    }
}