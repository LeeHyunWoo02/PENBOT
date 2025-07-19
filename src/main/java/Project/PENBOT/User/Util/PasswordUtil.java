package Project.PENBOT.User.Util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 생성자 주입을 통해 BCryptPasswordEncoder 주입
    public PasswordUtil(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 비밀번호 암호화
    public String encodePassword(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    // 비밀번호 일치 여부 확인
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
