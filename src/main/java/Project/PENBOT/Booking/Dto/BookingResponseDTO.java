package Project.PENBOT.Booking.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingResponseDTO {
    private boolean success;
    private int bookingId;
    private String message;

    public BookingResponseDTO(boolean success, int bookingId, String message) {
        this.success = success;
        this.bookingId = bookingId;
        this.message = message;
    }
}
