package Project.PENBOT.Host.Service;

import Project.PENBOT.Booking.Converter.BookingConverter;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Repository.BookingRepository;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.Host.Dto.BookingUpdateRequestDTO;
import Project.PENBOT.Host.Dto.BookingUpdateResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HostService {

    private final BookingRepository bookingRepository;

    public HostService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
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
    public BookingUpdateResponseDTO updateBooking(int bookingId, BookingUpdateRequestDTO requestDTO) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);

        Booking.builder()
                .status(requestDTO.getStatus())
                .headcount(requestDTO.getHeadcount())
                .endDate(requestDTO.getEndDate())
                .startDate(requestDTO.getStartDate())
                .build();

        bookingRepository.save(booking);
        return BookingUpdateResponseDTO.builder()
                .success(true)
                .message("예약 정보가 성공적으로 업데이트되었습니다.")
                .bookingId(booking.getId())
                .build();
    }
}
