package Project.PENBOT.Booking.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingRequestDTO {
    @Schema(description = "예약 시작 날짜 (yyyy-MM-dd)", example = "2025-08-10", required = true)
    private LocalDate startDate;

    @Schema(description = "예약 종료 날짜 (yyyy-MM-dd)", example = "2025-08-12", required = true)
    private LocalDate endDate;

    @Schema(description = "예약 인원 수 (예약 가능 확인 API에서는 제외 가능)", example = "4", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private int headcount;
}
