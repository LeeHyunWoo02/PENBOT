package Project.PENBOT.Booking.Converter;

import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.User.Entity.User;

public class BookingConverter {
    public static Booking toEntity(BookingRequestDTO requestDTO, User user) {
        return Booking.builder()
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .headcount(requestDTO.getHeadcount())
                .user(user)
                .status(BookStatus.PENDING)
                .build();
    }
}
