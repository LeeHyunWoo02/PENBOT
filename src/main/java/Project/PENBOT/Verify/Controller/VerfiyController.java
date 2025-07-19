package Project.PENBOT.Verify.Controller;

import Project.PENBOT.Verify.Dto.SmsRequestDto;
import Project.PENBOT.Verify.Dto.SmsResponseDto;
import Project.PENBOT.Verify.Service.VerifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerfiyController {
    private final VerifyService verifyService;

    public VerfiyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    private ResponseEntity<SmsResponseDto> sendAuthCode(@RequestBody SmsRequestDto request) {
        String toPhone = request.getToPhone();
        if( verifyService.sendAuthCodeAndSave(toPhone)){
            return ResponseEntity.ok(
                    new SmsResponseDto(true, "인증번호가 성공적으로 전송되었습니다.")
            );
        }
        return ResponseEntity.status(500).body(
                new SmsResponseDto(false, "인증번호 전송에 실패했습니다. 다시 시도해주세요.")
        );
    }
}
