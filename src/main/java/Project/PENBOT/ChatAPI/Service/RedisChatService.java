package Project.PENBOT.ChatAPI.Service;

import Project.PENBOT.ChatAPI.Dto.ChatMessageDTO;
import Project.PENBOT.User.Util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisChatService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson
    private final JwtUtil jwtUtil;
    private static final int MAX_MESSAGES = 10;
    private static final long AUTH_CODE_EXP_MINUTES = 5;


    // 메시지 추가 (최신 10개만 유지)
    public void addChatMessage(String auth, ChatMessageDTO chatMessageDTO) {
        String key = getKey(auth);
        try {
            String json = objectMapper.writeValueAsString(chatMessageDTO);
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
        } catch (JsonProcessingException e) {
            // 필요시 로깅/예외처리
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    // 최근 n개 메시지 조회 (JSON → ChatMessageDTO 역직렬화)
    public List<ChatMessageDTO> getRecentMessages(String key) {
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        List<ChatMessageDTO> result = new ArrayList<>();
        if (jsonList != null) {
            for (String json : jsonList) {
                try {
                    ChatMessageDTO dto = objectMapper.readValue(json, ChatMessageDTO.class);
                    result.add(dto);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("JSON 역직렬화 실패", e);
                }
            }
        }
        return result;
    }
    // 인증 코드 추가 (5분 후 만료)

    public void addAuthCode(String key, String authCode) {
        redisTemplate.opsForValue().set(key, authCode, AUTH_CODE_EXP_MINUTES, TimeUnit.MINUTES);
    }
    // 인증 코드 조회
    public String getAuthCode(String key){
        String code = redisTemplate.opsForValue().get(key);
        return code;
    }

    @NotNull
    private String getKey(String auth) {
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        return "chat:userId:" + userId;
    }
}
