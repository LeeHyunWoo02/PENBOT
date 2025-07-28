package Project.PENBOT.Host.Controller;

import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.Host.Dto.BookingUpdateRequestDTO;
import Project.PENBOT.Host.Service.HostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hosts")
public class HostController {

    private final HostService hostService;

    public HostController(HostService hostService) {
        this.hostService = hostService;
    }

    /**
     * 예약 상세 조회
     * */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<MyBookingResponseDTO> getBookingInfo(@PathVariable int bookingId){
        try{
            MyBookingResponseDTO responseDTO = hostService.getBookingInfo(bookingId);
            responseDTO.setSuccess(true);
            responseDTO.setMessage("예약 정보를 성공적으로 가져왔습니다.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MyBookingResponseDTO(false, null
                    ,"예약 정보를 가져오는 데 실패했습니다: " + e.getMessage()));
        }
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
            return ResponseEntity.ok(responseDTO);

        } catch (BookingNotFoundException e){
            return ResponseEntity.badRequest().body(
                    BookingResponseDTO.builder()
                            .success(false)
                            .message("예약 정보를 찾을 수 없습니다: " + e.getMessage())
                            .bookingId(0)
                            .build()
            );
        } catch (Exception e) {
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
        } catch (BookingNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    BookingResponseDTO.builder()
                            .success(false)
                            .message("예약 정보를 찾을 수 없습니다: " + e.getMessage())
                            .bookingId(0)
                            .build()
            );
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

}
