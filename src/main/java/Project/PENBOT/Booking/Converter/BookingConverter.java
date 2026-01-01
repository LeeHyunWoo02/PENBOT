package Project.PENBOT.Booking.Converter;

import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;



public class BookingConverter {

    public static Booking toEntity(BookingRequestDTO requestDTO) {

        return Booking.builder()
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .headcount(requestDTO.getHeadcount())
                .status(BookStatus.PENDING)
                .guestPhone(requestDTO.getGuestPhone())
                .guestName(requestDTO.getGuestName())
                .guestEmail(requestDTO.getGuestEmail())
                .password(requestDTO.getPassword())
                .build();
    }


    public static BookingSimpleDTO toDTO(Booking booking) {
        return BookingSimpleDTO.builder()
                .bookingId(booking.getId())
                .startDate(booking.getStartDate().toString())
                .endDate(booking.getEndDate().toString())
                .status(booking.getStatus())
                .headcount(booking.getHeadcount())
                .phone(booking.getGuestPhone())
                .name(booking.getGuestName())
                .build();
    }

}
