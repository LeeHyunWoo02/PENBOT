package Project.PENBOT.Booking.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingAvailableResponseDTO {
    private boolean available;
    private String message;

    public BookingAvailableResponseDTO(boolean available, String message) {
        this.available = available;
        this.message = message;
    }

}
