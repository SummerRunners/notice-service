package xyz.srunners.notice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import xyz.srunners.notice.dto.User;

public interface UserRepository extends MongoRepository<User, String> {
   }