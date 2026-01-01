package Project.PENBOT.Booking.Serivce;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.CustomException.*;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.Host.Repository.BlockedDateRepository;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BlockedDateRepository blockedDateRepository;

    public BookingService(BookingRepository bookingRepository, BlockedDateRepository blockedDateRepository) {
        this.bookingRepository = bookingRepository;
        this.blockedDateRepository = blockedDateRepository;

    }

    @Transactional
    public Booking createBooking(BookingRequestDTO requestDTO) {

        if (!isAvailable(requestDTO)) {
            throw new ForbiddenCreateBookingException("예약이 불가능한 날짜입니다.");
        }

        Booking booking = BookingConverter.toEntity(requestDTO);

        return bookingRepository.save(booking);
    }

    public boolean isAvailable(BookingRequestDTO requestDTO) {
        LocalDate start = requestDTO.getStartDate();
        LocalDate end = requestDTO.getEndDate();

        boolean isBooked = bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start);
        boolean isBlocked = blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start); // BlockedDate 존재 여부

        if(isBooked || isBlocked){
            throw new UnableBookingException();
        }
        return true;
    }



}
