package Project.PENBOT.Host.Service;

import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.CustomException.BlockedDateConflictException;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.Host.Dto.BlockDateRequestDTO;
import Project.PENBOT.Host.Dto.BlockedDateResponseDTO;
import Project.PENBOT.Host.Dto.BookingUpdateRequestDTO;
import Project.PENBOT.Host.Dto.UnavailableDateDTO;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HostServiceTest {

    @InjectMocks
    private HostService hostService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @Test
    @DisplayName("전체 예약 조회 실패 - 데이터가 없을 경우 예외 발생")
    void getBookingAll_Fail_Empty() {
        // Given
        given(bookingRepository.findAll()).willReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> hostService.getBookingAll())
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("예약이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예약 상태 변경 성공 (대기 -> 승인)")
    void updateBooking_Success() {
        // Given
        int bookingId = 1;
        BookingUpdateRequestDTO requestDTO = new BookingUpdateRequestDTO();
        requestDTO.setStatus(BookStatus.CONFIRMED);

        Booking booking = Booking.builder()
                .id(1)
                .status(BookStatus.PENDING) // 기존 상태
                .build();

        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

        // When
        BookingResponseDTO response = hostService.updateBooking(bookingId, requestDTO);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("업데이트되었습니다");

        assertThat(booking.getStatus()).isEqualTo(BookStatus.CONFIRMED);

        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("차단 날짜 생성 성공 - 겹치는 예약이 없을 때")
    void createBlockedDate_Success() {
        // Given
        BlockDateRequestDTO requestDTO = new BlockDateRequestDTO();
        requestDTO.setStartDate(LocalDate.of(2025, 5, 1));
        requestDTO.setEndDate(LocalDate.of(2025, 5, 5));
        requestDTO.setReason("내부 공사");

        given(bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .willReturn(false);
        given(blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .willReturn(false);

        BlockedDate savedDate = BlockedDate.builder().id(1).build();
        given(blockedDateRepository.save(any(BlockedDate.class))).willReturn(savedDate);

        // When
        BlockedDateResponseDTO response = hostService.createBlockedDate(requestDTO);

        // Then
        assertThat(response.isSuccess()).isTrue();
        verify(blockedDateRepository, times(1)).save(any(BlockedDate.class));
    }

    @Test
    @DisplayName("차단 날짜 생성 실패 - 이미 예약된 날짜와 겹칠 경우 예외 발생")
    void createBlockedDate_Fail_Conflict() {
        // Given
        BlockDateRequestDTO requestDTO = new BlockDateRequestDTO();
        requestDTO.setStartDate(LocalDate.of(2024, 5, 1));
        requestDTO.setEndDate(LocalDate.of(2024, 5, 5));

        given(bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> hostService.createBlockedDate(requestDTO))
                .isInstanceOf(BlockedDateConflictException.class);

        verify(blockedDateRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("불가능한 날짜 통합 조회 - 예약된 날짜와 차단된 날짜를 모두 반환")
    void getUnavailableDates_Success() {
        // Given
        Booking confirmedBooking = Booking.builder()
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 3))
                .status(BookStatus.CONFIRMED)
                .build();

        BlockedDate blockedDate = BlockedDate.builder()
                .id(10)
                .startDate(LocalDate.of(2025, 6, 10))
                .endDate(LocalDate.of(2025, 6, 12))
                .reason("공사")
                .build();

        given(bookingRepository.findAllByStatusIn(any())).willReturn(List.of(confirmedBooking));
        given(blockedDateRepository.findAll()).willReturn(List.of(blockedDate));

        // When
        List<UnavailableDateDTO> result = hostService.getUnavailableDates();

        // Then
        assertThat(result).hasSize(2);

        UnavailableDateDTO dto1 = result.get(0);
        assertThat(dto1.getType()).isEqualTo("BOOKED");
        assertThat(dto1.getStartDate()).isEqualTo(LocalDate.of(2025, 6, 1));

        UnavailableDateDTO dto2 = result.get(1);
        assertThat(dto2.getType()).isEqualTo("BLOCKED");
        assertThat(dto2.getReason()).isEqualTo("공사");
    }
}