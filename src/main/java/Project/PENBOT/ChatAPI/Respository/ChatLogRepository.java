package Project.PENBOT.ChatAPI.Respository;

import Project.PENBOT.ChatAPI.Entity.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatLogRepository extends JpaRepository<ChatLog, Integer> {

}
