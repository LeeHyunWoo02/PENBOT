package Project.PENBOT.Booking.Controller;

import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Serivce.BookingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/")
    public ResponseEntity<BookingResponseDTO> create(@RequestBody BookingRequestDTO requestDTO,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        Booking booking = bookingService.createBooking(requestDTO, auth);
        return ResponseEntity.ok(
                new BookingResponseDTO(true, booking.getId(), "예약이 성공적으로 생성되었습니다.")
        );
    }
}
