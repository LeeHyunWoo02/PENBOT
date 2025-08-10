package Project.PENBOT.Booking.Dto;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
public class MyBookingResponseDTO {

    private boolean success;
    private HashMap<String, BookingSimpleDTO> myBookings;
    private String message;

    public MyBookingResponseDTO(boolean success, @Nullable HashMap<String, BookingSimpleDTO> myBookings, @Nullable String message) {
        this.success = success;
        this.myBookings = myBookings;
        this.message = message;
    }

}
