package Project.PENBOT.Booking.Controller;

import Project.PENBOT.Booking.Dto.BookingAvailableResponseDTO;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.CustomException.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "예약 API", description = "예약 관련 API 제공")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "예약 생성", description = "예약을 생성함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 성공"),
            @ApiResponse(responseCode = "403", description = "이미 예약되어 있음"),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음"),
    })
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

    @Operation(summary = "예약 가능 여부 확인", description = "입력된 날짜 범위 내에서 예약이 가능한지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 가능 여부 확인 완료"),
            @ApiResponse(responseCode = "400", description = "요청 오류")
    })
    @GetMapping("/available")
    public ResponseEntity<BookingAvailableResponseDTO> isAvailable(@ModelAttribute BookingRequestDTO requestDTO) {
        try{
            bookingService.isAvailable(requestDTO);
            return ResponseEntity.ok(
                    new BookingAvailableResponseDTO(true, "예약이 가능합니다..")
            );
        } catch (UnableBookingException e){
            return ResponseEntity.badRequest().body(
                    new BookingAvailableResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new BookingAvailableResponseDTO(false, e.getMessage()));
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
