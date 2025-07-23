package Project.PENBOT.Booking.Controller;

import Project.PENBOT.Booking.Dto.BookingAvailableResponseDTO;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
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
}
