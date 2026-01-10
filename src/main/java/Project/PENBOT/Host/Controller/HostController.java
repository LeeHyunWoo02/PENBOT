package Project.PENBOT.Host.Controller;

import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.Host.Dto.*;
import Project.PENBOT.Host.Service.HostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/host")
public class HostController {

    private final HostService hostService;

    public HostController(HostService hostService) {
        this.hostService = hostService;
    }

    /**
     * 예약 모든 내역 조회
     * */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingListResponseDTO>> getAllBookings(){
        List<BookingListResponseDTO> response = hostService.getBookingAll();
        return ResponseEntity.ok(response);
    }

    /**
     * 예약 상세 조회
     * */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingSimpleDTO> getBookingInfo(@PathVariable int bookingId){
        BookingSimpleDTO responseDTO = hostService.getBookingInfo(bookingId);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 예약 업데이트
     * 날짜, 인원수 변경
     * 예약 상태 변경 ( 대기 -> 승인 )
     * */
    @PutMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponseDTO> updateBooking(@PathVariable int bookingId,
                                                            @RequestBody BookingUpdateRequestDTO request){
        try{
            BookingResponseDTO responseDTO = hostService.updateBooking(bookingId, request);
            log.info("예약 정보 업데이트 성공: {}", responseDTO);
            return ResponseEntity.ok(responseDTO);

        }catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    BookingResponseDTO.builder()
                            .success(false)
                            .message("예약 정보 업데이트에 실패했습니다: " + e.getMessage())
                            .bookingId(0)
                            .build()
            );
        }
    }

    /**
     * 예약 삭제
     * */
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponseDTO> deleteBooking(@PathVariable int bookingId){
        try{
            BookingResponseDTO responseDTO = hostService.deleteBooking(bookingId);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    BookingResponseDTO.builder()
                            .success(false)
                            .message("예약 삭제에 실패했습니다: " + e.getMessage())
                            .bookingId(0)
                            .build()
            );
        }
    }



    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<BlockedDateResponseDTO> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BlockedDateResponseDTO(false, "예약 정보가 없습니다.", 0));
    }
}
