package Project.PENBOT.Booking.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private int headcount;
}
