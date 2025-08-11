package Project.PENBOT.Host.Dto;


import Project.PENBOT.Booking.Entity.BookStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingUpdateRequestDTO {
    @Schema(description = "변경할 예약 시작 날짜", example = "2025-08-20", required = false)
    private LocalDate startDate;

    @Schema(description = "변경할 예약 종료 날짜", example = "2025-08-22", required = false)
    private LocalDate endDate;

    @Schema(description = "변경할 예약 인원 수", example = "3", required = false)
    private int headcount;

    @Schema(description = "예약 상태 (예: PENDING, APPROVED, REJECTED 등)", example = "APPROVED", required = false)
    private BookStatus status;
}
