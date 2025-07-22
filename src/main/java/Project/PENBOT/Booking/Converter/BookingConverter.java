package Project.PENBOT.Booking.Converter;

import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.User.Entity.User;

import java.util.HashMap;
import java.util.Set;

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

    public static MyBookingResponseDTO toDto(User user) {
        Set<Booking> bookings = user.getBookings();
        HashMap<String, BookingSimpleDTO> myBookings = new HashMap<>();
        for (Booking booking : bookings) {
            myBookings.put(
                    String.valueOf(booking.getId()),
                    BookingSimpleDTO.builder()
                            .startDate(booking.getStartDate().toString())
                            .endDate(booking.getEndDate().toString())
                            .status(booking.getStatus())
                            .headcount(booking.getHeadcount())
                            .build()
            );
        }
        return MyBookingResponseDTO.builder()
                .myBookings(myBookings)
                .build();

    }
}
