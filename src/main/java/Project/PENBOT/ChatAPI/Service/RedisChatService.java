package Project.PENBOT.ChatAPI.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisChatService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_MESSAGES = 10;
    private static final long AUTH_CODE_EXP_MINUTES = 5;


    // 메시지 추가 (최신 10개만 유지)
    public void addChatMessage(String key, String message) {
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
    }

    // 최근 n개 메시지 조회
    public List<String> getRecentMessages(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
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
}
