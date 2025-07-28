package Project.PENBOT.Host.Service;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.BookStatus;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.CustomException.BlockedDateConflictException;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.Host.Converter.BlockedDateConverter;
import Project.PENBOT.Host.Dto.BlockDateRequestDTO;
import Project.PENBOT.Host.Dto.BlockedDateResponseDTO;
import Project.PENBOT.Host.Dto.BookingUpdateRequestDTO;
import Project.PENBOT.Host.Dto.UnavailableDateDTO;
import Project.PENBOT.Host.Entity.BlockedDate;
import Project.PENBOT.Host.Repository.BlockedDateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class HostService {

    private final BookingRepository bookingRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final BookingService bookingService;

    public HostService(BookingRepository bookingRepository, BlockedDateRepository blockedDateRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.blockedDateRepository = blockedDateRepository;
        this.bookingService = bookingService;
    }

    /**
     * 예약 상세 조회
     * */
    public MyBookingResponseDTO getBookingInfo( int bookingId) {

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException();
        }

        return BookingConverter.toMyDto(bookingOptional.get());
    }

    /**
     * 예약 업데이트
     * 날짜, 인원수 변경
     * 예약 상태 변경 ( 대기 -> 승인 )
     * */
    @Transactional
    public BookingResponseDTO updateBooking(int bookingId, BookingUpdateRequestDTO requestDTO) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);

        Booking.builder()
                .status(requestDTO.getStatus())
                .headcount(requestDTO.getHeadcount())
                .endDate(requestDTO.getEndDate())
                .startDate(requestDTO.getStartDate())
                .build();

        bookingRepository.save(booking);
        return BookingResponseDTO.builder()
                .success(true)
                .message("예약 정보가 성공적으로 업데이트되었습니다.")
                .bookingId(booking.getId())
                .build();
    }

    /**
     * 예약 삭제
     * */
    @Transactional
    public BookingResponseDTO deleteBooking(int bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException();
        }

        bookingRepository.delete(bookingOptional.get());
        return BookingResponseDTO.builder()
                .success(true)
                .message("예약이 성공적으로 삭제되었습니다.")
                .bookingId(bookingId)
                .build();
    }

    public BlockedDateResponseDTO createBlockedDate(BlockDateRequestDTO requestDTO) {
        
        if (!isAvailable(requestDTO.getEndDate(), requestDTO.getStartDate())) {
            throw new BlockedDateConflictException();
        }

        BlockedDate blockedDate = BlockedDateConverter.toEntity(requestDTO);
        blockedDateRepository.save(blockedDate);

        return BlockedDateResponseDTO.builder()
                .success(true)
                .message("차단 날짜가 성공적으로 생성되었습니다.")
                .blockedDateId(blockedDate.getId())
                .build();

    }

    public List<UnavailableDateDTO> getUnavailableDates() {
        List<UnavailableDateDTO> unavailableDates = new ArrayList<>();

        // 예약된 날짜 → BOOKED
        List<BookStatus> statuses = Arrays.asList(BookStatus.CONFIRMED, BookStatus.PENDING);
        List<Booking> bookings = bookingRepository.findAllByStatusIn(statuses); // 예약 확정된 것만
        for (Booking booking : bookings) {
            unavailableDates.add(UnavailableDateDTO.builder()
                    .startDate(booking.getStartDate())
                    .endDate(booking.getEndDate())
                    .reason(booking.getStatus() == BookStatus.PENDING ? "예약 대기 중" : "예약 확정")
                    .type("BOOKED")
                    .build());
        }

        // 차단된 날짜 → BLOCKED
        List<BlockedDate> blockedDates = blockedDateRepository.findAll();
        for (BlockedDate blocked : blockedDates) {
            unavailableDates.add(UnavailableDateDTO.builder()
                    .startDate(blocked.getStartDate())
                    .endDate(blocked.getEndDate())
                    .reason(blocked.getReason())
                    .type("BLOCKED")
                    .build());
        }

        return unavailableDates;
    }
    public boolean isAvailable(LocalDate startDate, LocalDate endDate){
        boolean isBooked = bookingRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
        boolean isBlocked = blockedDateRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate); // BlockedDate 존재 여부

        return (isBooked || isBlocked);

    }
}
