package Project.PENBOT.Verify.Dto;

import lombok.Getter;

@Getter
public class SmsResponseDto {
    private final boolean success; // 인증번호 전송 성공 여부
    private final String message; // 인증번호 전송 결과 메시지

    public SmsResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
