package Project.PENBOT.ChatAPI.Respository;

import Project.PENBOT.ChatAPI.Entity.ChatLog;
import Project.PENBOT.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Integer> {
    List<ChatLog> findTop3ByUserAndBookingIsNullOrderByDateTime(User user);

}
