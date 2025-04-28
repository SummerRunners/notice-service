package xyz.srunners.notice.email.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import xyz.srunners.notice.email.exception.EmailException;

@Slf4j
@Service
public class ThymeleafEmailTemplateService implements EmailTemplateService {
    private final TemplateEngine templateEngine;
    
    public ThymeleafEmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    
    @Override
    public String generateTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.error("템플릿 생성 중 오류: ", e);
            throw new EmailException("이메일 템플릿 생성 중 오류가 발생했습니다.", e);
        }
    }
}