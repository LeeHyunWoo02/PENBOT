package Project.PENBOT.Verify.Controller;

import Project.PENBOT.Verify.Dto.SmsRequestDto;
import Project.PENBOT.Verify.Dto.SmsResponseDto;
import Project.PENBOT.Verify.Dto.VerifyRequestDto;
import Project.PENBOT.Verify.Service.VerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "휴대폰 인증 API", description = "휴대폰 인증번호 발송 및 검증 기능 제공")
@RestController
@RequestMapping("/api/verify")
public class VerfiyController {
    private final VerifyService verifyService;

    public VerfiyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    @Operation(summary = "인증번호 전송", description = "입력된 휴대폰 번호로 인증번호를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증번호 전송 성공"),
            @ApiResponse(responseCode = "500", description = "인증번호 전송 실패")
    })
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

    @Operation(summary = "인증번호 검증", description = "입력한 휴대폰 번호와 인증번호가 일치하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증번호 일치"),
            @ApiResponse(responseCode = "400", description = "인증번호 불일치")
    })
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
