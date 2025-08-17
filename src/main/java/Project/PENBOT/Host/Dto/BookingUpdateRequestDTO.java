package Project.PENBOT.Host.Dto;


import Project.PENBOT.Booking.Entity.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingUpdateRequestDTO {
    @Schema(description = "예약 상태 (예: PENDING, APPROVED, REJECTED 등)", example = "APPROVED", required = false)
    private BookStatus status;
}
