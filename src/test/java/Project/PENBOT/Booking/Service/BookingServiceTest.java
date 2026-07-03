package Project.PENBOT.Booking.Service;

import Project.PENBOT.Booking.Dto.BookingLookupRequestDTO;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.UnableBookingException;
import Project.PENBOT.Host.Entity.BlockedDate;
import Project.PENBOT.Host.Repository.BlockedDateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @Test
    @DisplayName("예약 생성 성공 - 겹치는 날짜가 없을 때")
    void createBooking_Success() {
        // Given
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 12);

        BookingRequestDTO requestDTO = new BookingRequestDTO();
        requestDTO.setStartDate(start);
        requestDTO.setEndDate(end);

        given(bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start))
                .willReturn(false);
        given(blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start))
                .willReturn(false);

        Booking savedBooking = Booking.builder()
                .id(1)
                .startDate(start)
                .endDate(end)
                .build();
        given(bookingRepository.save(any(Booking.class))).willReturn(savedBooking);

        // When
        Booking result = bookingService.createBooking(requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStartDate()).isEqualTo(start);

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 이미 예약된 날짜가 있을 때 예외 발생")
    void createBooking_Fail_AlreadyBooked() {
        // Given
        LocalDate start = LocalDate.of(2024, 1, 10);
        LocalDate end = LocalDate.of(2024, 1, 12);

        BookingRequestDTO requestDTO = new BookingRequestDTO();
        requestDTO.setStartDate(start);
        requestDTO.setEndDate(end);

        given(bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start))
                .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(requestDTO))
                .isInstanceOf(UnableBookingException.class);

        // verify
        verify(bookingRepository, times(0)).save(any(Booking.class));
    }

    @Test
    @DisplayName("예약 생성 실패 - 호스트가 막아둔(Blocked) 날짜일 때 예외 발생")
    void createBooking_Fail_BlockedDate() {
        // Given
        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 12);

        BookingRequestDTO requestDTO = new BookingRequestDTO();
        requestDTO.setStartDate(start);
        requestDTO.setEndDate(end);

        given(bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start))
                .willReturn(false); // 예약은 없음
        given(blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start))
                .willReturn(true); // 차단 날짜가 있음

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(requestDTO))
                .isInstanceOf(UnableBookingException.class);
    }

    @Test
    @DisplayName("예약 불가능한 날짜 조회 - 예약된 날짜와 막힌 날짜를 합쳐서 반환")
    void getUnavailableDates_Success() {
        // Given
        int year = 2025;
        int month = 1;
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        Booking booking = Booking.builder()
                .startDate(LocalDate.of(2025, 1, 2))
                .endDate(LocalDate.of(2025, 1, 4))
                .build();

        BlockedDate blockedDate = BlockedDate.builder()
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 1, 10))
                .build();

        given(bookingRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(monthEnd, monthStart))
                .willReturn(List.of(booking));
        given(blockedDateRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(monthEnd, monthStart))
                .willReturn(List.of(blockedDate));

        // When
        List<String> unavailableDates = bookingService.getUnavailableDates(year, month);

        // Then
        assertThat(unavailableDates).hasSize(3);
        assertThat(unavailableDates).contains("2025-01-02", "2025-01-03", "2025-01-10");
        assertThat(unavailableDates).doesNotContain("2025-01-04"); // 체크아웃 날짜는 예약 가능해야 함
    }

    @Test
    @DisplayName("예약 조회 성공 - 이름/전화번호/비밀번호가 모두 일치할 때")
    void checkMyBooking_Success() {
        // Given
        BookingLookupRequestDTO requestDTO = new BookingLookupRequestDTO();
        requestDTO.setGuestName("홍길동");
        requestDTO.setGuestPhone("01012345678");
        requestDTO.setPassword(1234);

        Booking booking = Booking.builder()
                .id(1)
                .startDate(LocalDate.of(2026, 1, 10))
                .endDate(LocalDate.of(2026, 1, 12))
                .headcount(4)
                .status(BookStatus.CONFIRMED)
                .guestName("홍길동")
                .guestPhone("01012345678")
                .password(1234)
                .build();

        given(bookingRepository.findByGuestNameAndGuestPhoneAndPassword("홍길동", "01012345678", 1234))
                .willReturn(List.of(booking));

        // When
        HashMap<String, BookingSimpleDTO> result = bookingService.checkMyBooking(requestDTO);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get("1").getStatus()).isEqualTo(BookStatus.CONFIRMED);
        assertThat(result.get("1").getStartDate()).isEqualTo("2026-01-10");
    }

    @Test
    @DisplayName("예약 조회 실패 - 일치하는 예약이 없을 때 예외 발생")
    void checkMyBooking_Fail_NotFound() {
        // Given
        BookingLookupRequestDTO requestDTO = new BookingLookupRequestDTO();
        requestDTO.setGuestName("홍길동");
        requestDTO.setGuestPhone("01012345678");
        requestDTO.setPassword(9999);

        given(bookingRepository.findByGuestNameAndGuestPhoneAndPassword("홍길동", "01012345678", 9999))
                .willReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> bookingService.checkMyBooking(requestDTO))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
