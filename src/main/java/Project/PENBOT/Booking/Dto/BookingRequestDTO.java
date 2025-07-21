package Project.PENBOT.Booking.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequestDTO {
    private String startDate;
    private String endDate;
    private int headcount;
}
