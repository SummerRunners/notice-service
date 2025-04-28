package xyz.srunners.notice.email.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.srunners.notice.email.exception.EmailException;

@SpringBootTest
class ThymeleafEmailTemplateServiceTests {
    
    @Autowired
    private EmailTemplateService emailTemplateService;
    
    @Test
     void testGenerateTemplate() {
        // Given
        String templateName = "mail/welcome";
        Map<String, Object> variables = new HashMap<>();
        variables.put("subject", "테스트 제목");
        variables.put("content", "<h1>테스트 내용</h1>");
        variables.put("email", "test@example.com");
        
        // When
        String result = emailTemplateService.generateTemplate(templateName, variables);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("테스트 제목"));
        assertTrue(result.contains("<h1>테스트 내용</h1>"));
        assertTrue(result.contains("test@example.com"));
    }
    
    @Test
    void testGenerateTemplateWithInvalidTemplate() {
        // Given
        String invalidTemplate = "invalid-template";
        Map<String, Object> variables = new HashMap<>();
        
        // When & Then
        assertThrows(EmailException.class, () ->
            emailTemplateService.generateTemplate(invalidTemplate, variables)
        );
    }
}