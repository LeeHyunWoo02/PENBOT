package Project.PENBOT.Verify.Controller;

import Project.PENBOT.Verify.Dto.SmsRequestDto;
import Project.PENBOT.Verify.Dto.SmsResponseDto;
import Project.PENBOT.Verify.Dto.VerifyRequestDto;
import Project.PENBOT.Verify.Service.VerifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verify")
public class VerfiyController {
    private final VerifyService verifyService;

    public VerfiyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    @PostMapping("/sendcode")
    private ResponseEntity<SmsResponseDto> sendAuthCode(@RequestBody SmsRequestDto request) {
        String toPhone = request.getPhone();
        if( verifyService.sendAuthCodeAndSave(toPhone)){
            return ResponseEntity.ok(
                    new SmsResponseDto(true, "인증번호가 성공적으로 전송되었습니다.")
            );
        }
        return ResponseEntity.status(500).body(
                new SmsResponseDto(false, "인증번호 전송에 실패했습니다. 다시 시도해주세요.")
        );
    }

    @PostMapping("/verifycode")
    private ResponseEntity<SmsResponseDto> sendSmsRequest(@RequestBody VerifyRequestDto request) {
        String phone = request.getPhone();
        String code = request.getCode();

        if (verifyService.verifyCode(phone, code)) {
            return ResponseEntity.ok(
                    new SmsResponseDto(true, "인증번호가 일치합니다.")
            );
        } else {
            return ResponseEntity.status(400).body(
                    new SmsResponseDto(false, "인증번호가 일치하지 않습니다.")
            );
        }
    }
}
