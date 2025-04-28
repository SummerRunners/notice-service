package xyz.srunners.notice.email.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import xyz.srunners.notice.email.dto.MailLog;

@Repository
public interface MailLogRepository extends MongoRepository<MailLog, String> {
    List<MailLog> findAllByIsBulkTrue();

    Optional<MailLog> findByRecipient(String testEmail);

    Optional<Object> findFirstByRecipientOrderByRequestedAtDesc(String email);

    List<MailLog> findAllByJobId(String jobId);
}