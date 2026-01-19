package Project.PENBOT.Booking.Service;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.CustomException.*;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.Host.Entity.BlockedDate;
import Project.PENBOT.Host.Repository.BlockedDateRepository;


import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BlockedDateRepository blockedDateRepository;

    public BookingService(BookingRepository bookingRepository, BlockedDateRepository blockedDateRepository) {
        this.bookingRepository = bookingRepository;
        this.blockedDateRepository = blockedDateRepository;
    }

    // 예약 생성
    @Transactional
    public Booking createBooking(BookingRequestDTO requestDTO) {

        if (!isAvailable(requestDTO)) {
            throw new ForbiddenCreateBookingException("예약이 불가능한 날짜입니다.");
        }

        Booking booking = BookingConverter.toEntity(requestDTO);

        return bookingRepository.save(booking);
    }

    // 예약 가능한지 확인
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

    // 예약 불가능한 날짜 확인
    public List<String> getUnavailableDates(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<Booking> bookings = bookingRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(monthEnd, monthStart);
        List<BlockedDate> blockedDates = blockedDateRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(monthEnd, monthStart);

        Set<String> unavailableDates = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Booking booking : bookings) {
            addDatesToSet(booking.getStartDate(), booking.getEndDate(), monthStart, monthEnd, unavailableDates, formatter);
        }

        for (BlockedDate blocked : blockedDates) {
            addDatesToSet(blocked.getStartDate(), blocked.getEndDate().plusDays(1), monthStart, monthEnd, unavailableDates, formatter);
        }

        return new ArrayList<>(unavailableDates);
    }

    private void addDatesToSet(LocalDate start, LocalDate targetEnd,
                               LocalDate monthStart, LocalDate monthEnd,
                               Set<String> targetSet, DateTimeFormatter formatter) {

        LocalDate current = start.isBefore(monthStart) ? monthStart : start;

        /**
         * 체크아웃 날짜 전까지 block, 해당 월을 넘어가지 않게 함.
         * */
        while (current.isBefore(targetEnd) && !current.isAfter(monthEnd)) {
            targetSet.add(current.format(formatter));
            current = current.plusDays(1);
        }
    }


}
