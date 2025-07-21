package Project.PENBOT.Booking.Converter;

import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.User.Entity.User;

public class BookingConverter {
    public static Booking toEntity(BookingRequestDTO requestDTO, User user) {
        return Booking.builder()
                .start_date(requestDTO.getStartDate())
                .end_date(requestDTO.getEndDate())
                .headcount(requestDTO.getHeadcount())
                .user(user)
                .build();
    }
}
