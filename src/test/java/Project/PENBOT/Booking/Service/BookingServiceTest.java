package Project.PENBOT.Booking.Service;

import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
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

        // 예약 날짜 1월 2일 ~ 1월 4일
        Booking booking = Booking.builder()
                .startDate(LocalDate.of(2025, 1, 2))
                .endDate(LocalDate.of(2025, 1, 4))
                .build();

        // 블락 날짜 1워 10일
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
}
