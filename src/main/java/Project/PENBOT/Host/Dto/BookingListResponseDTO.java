package Project.PENBOT.Host.Dto;

import Project.PENBOT.Booking.Entity.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingListResponseDTO {
    private int bookingId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private int headcount;
    private BookStatus status;
}
