package Project.PENBOT.Booking.Controller;

import Project.PENBOT.Booking.Dto.BookingAvailableResponseDTO;
import Project.PENBOT.Booking.Dto.BookingRequestDTO;
import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.Booking.Serivce.BookingService;
import Project.PENBOT.ChatAPI.Service.ChatLogService;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.ForbiddenCreateBookingException;
import Project.PENBOT.CustomException.ForbiddenException;
import Project.PENBOT.CustomException.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final ChatLogService chatLogService;
    public BookingController(BookingService bookingService, ChatLogService chatLogService) {
        this.bookingService = bookingService;
        this.chatLogService = chatLogService;
    }

    @Operation(summary = "예약 생성", description = "예약을 생성하고 채팅 로그도 함께 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 성공"),
            @ApiResponse(responseCode = "403", description = "이미 예약되어 있음"),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음"),
    })
    @PostMapping("/")
    public ResponseEntity<BookingResponseDTO> create(@RequestBody BookingRequestDTO requestDTO,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        try{
            Booking booking = bookingService.createBooking(requestDTO, auth);
            chatLogService.BookingChatSave(booking.getId());
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

    @Operation(summary = "예약 목록 조회", description = "로그인된 사용자의 모든 예약 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 실패")
    })
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

    @Operation(summary = "단일 예약 조회", description = "특정 예약 ID에 대한 예약 상세 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 유저, 예약을 찾을 수 없음")
    })
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


    @Operation(summary = "예약 삭제", description = "예약을 취소(삭제)합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "예약 삭제 실패")
    })
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

    @ExceptionHandler(ForbiddenCreateBookingException.class)
    public ResponseEntity<BookingResponseDTO> handleForbiddenCreateBooking(ForbiddenCreateBookingException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BookingResponseDTO(false, 0, ex.getMessage()));
    }
}
