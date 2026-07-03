package Project.PENBOT.Booking.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingLookupRequestDTO {
    @Schema(description = "예약자 성함", example = "홍길동", required = true)
    private String guestName;

    @Schema(description = "예약자 전화번호", example = "01012345678", required = true)
    private String guestPhone;

    @Schema(description = "예약 확인 비밀번호 (숫자 4자리)", example = "1234", required = true)
    private Integer password;
}
