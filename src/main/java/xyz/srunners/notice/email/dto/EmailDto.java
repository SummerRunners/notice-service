package xyz.srunners.notice.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EmailDto {

    @Getter
    @Builder
    public static class SingleEmailRequest {
        @Email
        @NotBlank
        private final String to;

        @NotBlank
        private final String subject;

        @NotBlank
        private final String content;

        public Map<String, Object> toTemplateVariables() {
            Map<String, Object> variables = new HashMap<>();
            variables.put("email", to);
            variables.put("subject", subject);
            variables.put("content", content);
            return variables;
        }
    }

    @Getter
    @Builder
    public static class BulkEmailRequest {
        @NotEmpty
        private final List<@Email @NotBlank String> recipients;

        @NotBlank
        private final String subject;

        @NotBlank
        private final String content;
    }
}