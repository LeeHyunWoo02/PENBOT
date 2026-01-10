package Project.PENBOT.Booking.Serivce;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.CustomException.*;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
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

    public List<String> getUnavailableDates(int year, int month) {
        // 1. 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // 2. 해당 기간에 겹치는 모든 예약(및 블락된 날짜) 조회
        List<Booking> bookings = bookingRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(monthEnd, monthStart);

        // (BlockedDate 로직도 있다면 동일하게 조회해서 합칩니다)

        // 3. 예약된 날짜들을 "yyyy-MM-dd" 문자열로 변환하여 Set에 담기 (중복 제거)
        Set<String> unavailableDates = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Booking booking : bookings) {
            // 예약 시작일과 해당 월의 시작일 중 늦은 날짜부터 시작 (전달에서 넘어온 예약 처리)
            LocalDate current = booking.getStartDate().isBefore(monthStart) ? monthStart : booking.getStartDate();
            // 예약 종료일과 해당 월의 종료일 중 빠른 날짜까지 (다음달로 넘어가는 예약 처리)
            LocalDate end = booking.getEndDate().isAfter(monthEnd) ? monthEnd : booking.getEndDate();

            while (!current.isAfter(end)) {
                unavailableDates.add(current.format(formatter));
                current = current.plusDays(1);
            }
        }

        return new ArrayList<>(unavailableDates);
    }



}
