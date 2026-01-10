package Project.PENBOT.Booking.Controller;

import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.CustomException.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "예약 API", description = "예약 관련 API 제공")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/")
    public ResponseEntity<BookingResponseDTO> create(@RequestBody BookingRequestDTO requestDTO) {
        try{
            Booking booking = bookingService.createBooking(requestDTO);
            return ResponseEntity.ok(
                    new BookingResponseDTO(true, booking.getId(), "예약이 성공적으로 생성되었습니다.")
            );
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new BookingResponseDTO(false, 0, e.getMessage()));
        }
    }

    @GetMapping("/unavailable")
    public ResponseEntity<List<String>> getUnavailableDates(
            @RequestParam int year,
            @RequestParam int month
    ) {
        try {
            List<String> dates = bookingService.getUnavailableDates(year, month);
            return ResponseEntity.ok(dates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<BookingResponseDTO> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BookingResponseDTO(false, 0, ex.getMessage()));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<BookingResponseDTO> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BookingResponseDTO(false, 0, ex.getMessage()));
    }


    @ExceptionHandler(ForbiddenCreateBookingException.class)
    public ResponseEntity<BookingResponseDTO> handleForbiddenCreateBooking(ForbiddenCreateBookingException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BookingResponseDTO(false, 0, ex.getMessage()));
    }
}
