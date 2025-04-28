package xyz.srunners.notice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalController {

    @ModelAttribute("requestURI")
    String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
