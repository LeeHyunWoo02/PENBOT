package Project.PENBOT.Verify.Service;

import Project.PENBOT.Verify.Util.CoolSMSUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerifyServiceTest {

    @InjectMocks
    private VerifyService verifyService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private CoolSMSUtil coolsms;

    @Test
    @DisplayName("인증번호 발송 및 저장 성공")
    void sendAuthCodeAndSave_Success() {
        // Given
        String phone = "01012345678";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        willDoNothing().given(valueOperations).set(any(), any(), any(Duration.class));
        willDoNothing().given(coolsms).sendAuthCode(any(), any());

        // When
        boolean result = verifyService.sendAuthCodeAndSave(phone);

        // Then
        assertThat(result).isTrue();

        verify(valueOperations, times(1)).set(eq("sms:auth:" + phone), any(), any(Duration.class));
        verify(coolsms, times(1)).sendAuthCode(eq(phone), any());
    }

    @Test
    @DisplayName("인증번호 발송 실패 - 예외 발생 시 false 반환")
    void sendAuthCodeAndSave_Fail_Exception() {
        // Given
        String phone = "01012345678";

        willThrow(new RuntimeException("SMS 발송 실패")).given(coolsms).sendAuthCode(any(), any());

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // When
        boolean result = verifyService.sendAuthCodeAndSave(phone);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("인증번호 검증 성공 - 코드가 일치할 때")
    void verifyCode_Success() {
        // Given
        String phone = "01012345678";
        String correctCode = "123456";
        String key = "sms:auth:" + phone;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        given(valueOperations.get(key)).willReturn(correctCode);

        // When
        boolean result = verifyService.verifyCode(phone, correctCode);

        // Then
        assertThat(result).isTrue();

        verify(redisTemplate, times(1)).delete(key);
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 코드가 일치하지 않을 때")
    void verifyCode_Fail_WrongCode() {
        // Given
        String phone = "01012345678";
        String savedCode = "123456";
        String wrongInput = "000000";
        String key = "sms:auth:" + phone;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(savedCode);

        // When
        boolean result = verifyService.verifyCode(phone, wrongInput);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate, times(0)).delete(key);
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 인증 시간 만료 (Redis에 데이터 없음)")
    void verifyCode_Fail_Expired() {
        // Given
        String phone = "01012345678";
        String inputCode = "123456";
        String key = "sms:auth:" + phone;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(null);

        // When
        boolean result = verifyService.verifyCode(phone, inputCode);

        // Then
        assertThat(result).isFalse();
    }
}