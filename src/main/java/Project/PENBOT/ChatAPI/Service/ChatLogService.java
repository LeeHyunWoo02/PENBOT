package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.ChatAPI.Converter.ChatLogConverter;
import Project.PENBOT.ChatAPI.Dto.QueryRequestDTO;
import Project.PENBOT.ChatAPI.Dto.QueryResponseDTO;
import Project.PENBOT.ChatAPI.Entity.ChatLog;
import Project.PENBOT.ChatAPI.Respository.ChatLogRepository;
import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ChatLogService {
    private final ChatLogRepository chatLogRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ChatLogService(ChatLogRepository chatLogRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.chatLogRepository = chatLogRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ChatLog saveUserChat(String auth, QueryRequestDTO requestDTO) {
        int userId = getUserId(auth);
        try{
            User user = userRepository.findById(userId);
            ChatLog chatlog = ChatLogConverter.UsertoEntity(requestDTO, user);
            user.addChatLog(chatlog);
            return chatLogRepository.save(chatlog);
        } catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
    }

    @Transactional
    public ChatLog saveBotChat(String auth, String answer){
        int userId = getUserId(auth);
        try{
            User user = userRepository.findById(userId);
            ChatLog chatlog = ChatLogConverter.BottoEntity(answer, user);
            user.addChatLog(chatlog);
            return chatLogRepository.save(chatlog);
        } catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
    }

    private int getUserId(String auth) {
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        return userId;
    }
}
