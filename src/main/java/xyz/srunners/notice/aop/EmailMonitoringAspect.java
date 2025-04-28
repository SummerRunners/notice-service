package xyz.srunners.notice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class EmailMonitoringAspect {
    
    @Around("execution(* xyz.srunners.notice.email.service.*.*(..))")
    public Object monitorEmailOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("메서드 {} 실행 완료 - 소요시간: {}ms", methodName, executionTime);
            return result;
        } catch (Exception e) {
            log.error("메서드 {} 실행 중 오류: {}", methodName, e.getMessage());
            throw e;
        }
    }
}