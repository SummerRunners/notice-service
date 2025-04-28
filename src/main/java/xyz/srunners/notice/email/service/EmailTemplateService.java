package xyz.srunners.notice.email.service;

import java.util.Map;

public interface EmailTemplateService {
    String generateTemplate(String templateName, Map<String, Object> variables);
}