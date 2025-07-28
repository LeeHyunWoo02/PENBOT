package Project.PENBOT.Host.Dto;


import Project.PENBOT.Booking.Entity.BookStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingUpdateRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private int headcount;
    private BookStatus status;
}
