package Project.PENBOT.Verify.Dto;

import lombok.Getter;

@Getter
public class VerifyRequestDto {
    private final String phone;
    private final String code;


    public VerifyRequestDto(String phone, String code) {
        this.phone = phone;
        this.code = code;
    }
}
