package xyz.srunners.notice.email.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "mail_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "status_requestedAt", def = "{'status': 1, 'requestedAt': -1}"),
    @CompoundIndex(name = "recipient_requestedAt", def = "{'recipient': 1, 'requestedAt': -1}")
})
public class MailLog {
    @Id
    private String id;

    private String jobId;

    @Indexed
    private String recipient;

    private String subject;

    private String content;

    private boolean isHtml;

    private boolean isBulk;

    @Indexed
    private MailStatus status;

    private String errorMessage;

    @Indexed
    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;

    @Field("metadata")
    private Map<String, Object> metadata;
}