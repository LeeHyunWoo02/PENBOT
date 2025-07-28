package Project.PENBOT.Host.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookingUpdateResponseDTO {
    private boolean success;
    private String message;
    private int bookingId;
}
