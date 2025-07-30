package Project.PENBOT.Host.Controller;

import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.Host.Dto.BlockedDateResponseDTO;
import Project.PENBOT.Host.Dto.BookingUpdateRequestDTO;
import Project.PENBOT.Host.Dto.UserDetailResponseDTO;
import Project.PENBOT.Host.Dto.UserListResponseDTO;
import Project.PENBOT.Host.Service.HostService;
import Project.PENBOT.User.Dto.UserResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 관리자 용 모든 유저 조회
     * */
    @GetMapping("/users")
    public ResponseEntity<List<UserListResponseDTO>> getAllUser(){
        List<UserListResponseDTO> allUsers = hostService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    /**
     * 유저 상세 조회 ( 관리자용 )
     * */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailResponseDTO> getDetailUser(@PathVariable int userId){
        UserDetailResponseDTO responseDTO = hostService.getUserDetail(userId);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 관리자 권한으로 유저 정보 삭제
     * */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> deleteUser(@PathVariable int userId){
        try{
            UserResponseDTO responseDTO = hostService.deleteUser(userId);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new UserResponseDTO(false, "사용자 삭제에 실패했습니다: " + e.getMessage())
            );
        }
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserResponseDTO> handleBlockedDateConflict(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new UserResponseDTO(false, ex.getMessage()));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<BlockedDateResponseDTO> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BlockedDateResponseDTO(false, "예약 정보가 없습니다.", 0));
    }
}
