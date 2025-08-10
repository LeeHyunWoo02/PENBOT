package Project.PENBOT.Booking.Dto;

import Project.PENBOT.Booking.Entity.BookStatus;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingSimpleDTO {
    private String startDate;
    private String endDate;
    private int headcount;
    private BookStatus status;

    @Nullable
    private String name;
    @Nullable
    private String phone;
}
