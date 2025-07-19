package Project.PENBOT.Verify.Service;

import Project.PENBOT.Verify.Util.CoolSMSUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class VerifyService {

    private final StringRedisTemplate redisTemplate;
    private final CoolSMSUtil coolsms;

    private static final long AUTH_CODE_EXP_MINUTES = 5;

    public VerifyService(StringRedisTemplate redisTemplate, CoolSMSUtil coolsms) {
        this.redisTemplate = redisTemplate;
        this.coolsms = coolsms;
    }

    /**
     * 통합 처리 메소드
     * */
    public void sendAuthCodeAndSave(String phone) {
        String code = generateCode();

        saveAuthCode(phone, code);

        sendSMS(phone, code);
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
        redisTemplate.opsForValue().set(key, code, AUTH_CODE_EXP_MINUTES, TimeUnit.MINUTES); // 5분 동안 유효
    }

    /**
     * 인증코드 검증 메소드
     * */
    public boolean verifyCode(String phone, String code){
        String key = buildKey(phone);
        String savedCode = redisTemplate.opsForValue().get(key);

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
