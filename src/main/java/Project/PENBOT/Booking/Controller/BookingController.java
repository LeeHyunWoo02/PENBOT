package Project.PENBOT.Booking.Controller;

import Project.PENBOT.Booking.Dto.BookingAvailableResponseDTO;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.ForbiddenException;
import Project.PENBOT.CustomException.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
        try{
            Booking booking = bookingService.createBooking(requestDTO, auth);
            return ResponseEntity.ok(
                    new BookingResponseDTO(true, booking.getId(), "예약이 성공적으로 생성되었습니다.")
            );
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new BookingResponseDTO(false, 0, e.getMessage()));
        }

    }

    @GetMapping("/available")
    public ResponseEntity<BookingAvailableResponseDTO> isAvailable(@RequestBody BookingRequestDTO requestDTO) {
        try{
            bookingService.isAvailable(requestDTO);
            return ResponseEntity.ok(
                    new BookingAvailableResponseDTO(true, "예약 가능 여부를 확인했습니다.")
            );
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new BookingAvailableResponseDTO(false, e.getMessage()));
        }
    }

    @GetMapping("/myall")
    public ResponseEntity<MyBookingResponseDTO> getMyBookings(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth){
        try {
            MyBookingResponseDTO responseDTO = bookingService.getAllMyBooking(auth);
            responseDTO.setSuccess(true);
            responseDTO.setMessage("예약 정보를 성공적으로 가져왔습니다.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MyBookingResponseDTO(false, null
                    ,"예약 정보를 가져오는 데 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<MyBookingResponseDTO> getMyBooking(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
                                                             @PathVariable int bookingId) {
        try{
            MyBookingResponseDTO responseDTO = bookingService.getMyBooking(auth,bookingId);
            responseDTO.setSuccess(true);
            responseDTO.setMessage("예약 정보를 성공적으로 가져왔습니다.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MyBookingResponseDTO(false, null
                    ,"예약 정보를 가져오는 데 실패했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{bookingId}/delete")
    public ResponseEntity<BookingResponseDTO> deleteBooking(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
                                                             @PathVariable int bookingId) {
        try{
            bookingService.deleteBooking(auth, bookingId);
            return ResponseEntity.ok(new BookingResponseDTO(true, bookingId, "예약이 성공적으로 삭제되었습니다."));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new BookingResponseDTO(false, bookingId, e.getMessage()));
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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BookingResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BookingResponseDTO(false, 0, ex.getMessage()));
    }
}
