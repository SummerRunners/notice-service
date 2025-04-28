package xyz.srunners.notice.email.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.srunners.notice.email.dto.MailLog;
import xyz.srunners.notice.email.repository.MailLogRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailViewController {

    private final MailLogRepository mailLogRepository;
    /**
     * 이메일 작성 페이지를 제공합니다.
     *
     * @return 이메일 작성 템플릿 경로
     */
    @GetMapping("/compose")
    public String showComposeForm() {
        log.debug("이메일 작성 페이지 요청");
        return "mail/compose";
    }

    @GetMapping("/logs")
    public String viewMailLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        // 요청 날짜 기준 내림차순으로 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        
        // 대량 이메일 작업 목록 조회
        List<MailLog> bulkLogs = mailLogRepository.findAllByIsBulkTrue();
        
        // 모든 이메일 로그를 페이징하여 조회
        Page<MailLog> allLogs = mailLogRepository.findAll(pageable);
        
        // 모델에 데이터 추가
        model.addAttribute("bulkLogs", bulkLogs);
        model.addAttribute("allLogs", allLogs);
        model.addAttribute("currentPage", page);
        
        return "mail/logs";
    }
    
    @GetMapping("/{id}")
    public String viewMailLogDetail(@PathVariable String id, Model model) {
        Optional<MailLog> mailLog = mailLogRepository.findById(id);
        
        if (mailLog.isPresent()) {
            MailLog log = mailLog.get();
            
            // 실행 시간 계산
            long executionTime = 0;
            if (log.getMetadata() != null && log.getMetadata().containsKey("executionTimeMs")) {
                executionTime = Long.parseLong(log.getMetadata().get("executionTimeMs").toString());
            }
            
            // 관련 이메일 로그 (같은 벌크 작업에 속한 로그들)
            List<MailLog> relatedLogs = null;
            if (log.isBulk() && log.getJobId() != null) {
                relatedLogs = mailLogRepository.findAllByJobId(log.getJobId());
            }
            
            model.addAttribute("mailLog", log);
            model.addAttribute("executionTime", formatDuration(executionTime));
            model.addAttribute("relatedLogs", relatedLogs);
            
            return "mail/log-detail";
        }
        
        return "redirect:/mail/logs";
    }
    
    // 밀리초를 보기 좋은 형식으로 변환 (1m 30s 등)
    private String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(millis);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        
        if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }
}