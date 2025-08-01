package Project.PENBOT.Verify.Service;

import Project.PENBOT.ChatAPI.Service.RedisChatService;
import Project.PENBOT.Verify.Util.CoolSMSUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@AllArgsConstructor
public class VerifyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisChatService redisChatService;
    private final CoolSMSUtil coolsms;



    /**
     * 통합 처리 메소드
     * */
    public boolean sendAuthCodeAndSave(String phone) {
        try{
            String code = generateCode();
            saveAuthCode(phone, code);
            sendSMS(phone, code);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 예외 발생 시 false 반환
        }
        return true;
    }

    /**
     * 인증코드 생성 메소드
     * */
    public String generateCode(){
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * SMS 인증 코드 전송 메서드
     * */
    public void sendSMS(String ToPhone, String code){
        coolsms.sendAuthCode(ToPhone, code); // CoolSMSUtil을 통해 인증 코드 전송
    }

    /**
     * 인증코드 Redis 저장 메서드
     * */
    public void saveAuthCode(String phone, String code){
        String key = buildKey(phone);
        redisChatService.addAuthCode(key,code); // 5분 동안 유효
    }

    /**
     * 인증코드 검증 메소드
     * */
    public boolean verifyCode(String phone, String code){
        String key = buildKey(phone);
        String savedCode = redisChatService.getAuthCode(key);

        if (savedCode != null && savedCode.equals(code)){
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String buildKey(String phone) {
        return "sms:auth:" + phone;
    }
}
