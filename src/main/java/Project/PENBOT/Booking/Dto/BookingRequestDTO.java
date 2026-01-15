package Project.PENBOT.Booking.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 예약 생성 API 요청시에는 headcount까지 확인
 * 예약 가능 날짜 확인 API 는 headcount 제외
 * */
@Getter
@Setter
public class BookingRequestDTO {
    @Schema(description = "예약 시작 날짜 (yyyy-MM-dd)", example = "2025-08-10", required = true)
    private LocalDate startDate;

    @Schema(description = "예약 종료 날짜 (yyyy-MM-dd)", example = "2025-08-12", required = true)
    private LocalDate endDate;

    @Schema(description = "예약 인원 수 (예약 가능 확인 API에서는 제외 가능)", example = "4", required = true)
    private int headcount;

    @Schema(description = "예약 비밀번호 (숫자)", example = "1234", required = true)
    private Integer password;

    @Schema(description = "게스트 이름", example = "홍길동", required = true)
    private String guestName;

    @Schema(description = "게스트 전화번호", example = "010-1234-5678", required = true)
    private String guestPhone;

    @Schema(description = "게스트 이메일", example = "exmaple@naver.com")
    private String guestEmail;
}
