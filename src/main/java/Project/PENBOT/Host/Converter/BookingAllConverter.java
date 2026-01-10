package Project.PENBOT.Host.Converter;

import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Host.Dto.BookingListResponseDTO;

import java.util.List;

public class BookingAllConverter {
    public static List<BookingListResponseDTO> toDTO(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> BookingListResponseDTO.builder()
                        .bookingId(booking.getId())
                        .startDate(booking.getStartDate())
                        .endDate(booking.getEndDate())
                        .headcount(booking.getHeadcount())
                        .status(booking.getStatus())
                        .guestName(booking.getGuestName())
                        .guestPhone(booking.getGuestPhone())
                        .build())
                .toList();
    }
}
