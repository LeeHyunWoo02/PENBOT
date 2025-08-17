package Project.PENBOT.Host.Controller;

import Project.PENBOT.Booking.Dto.BookingResponseDTO;
import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.Booking.Dto.MyBookingResponseDTO;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.Host.Dto.*;
import Project.PENBOT.Host.Service.HostService;
import Project.PENBOT.User.Dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "호스트(관리자) API", description = "관리자용 예약 관리, 유저 관리 기능 제공")
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
    @Operation(summary = "예약 모든 내역 조회", description = "예약 모든 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "예약 정보 조회 실패")
    })
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingListResponseDTO>> getAllBookings(){
        List<BookingListResponseDTO> response = hostService.getBookingAll();
        return ResponseEntity.ok(response);
    }

    /**
     * 예약 상세 조회
     * */
    @Operation(summary = "예약 상세 조회", description = "예약 ID로 예약 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "예약 정보 조회 실패")
    })
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
    @Operation(summary = "예약 수정", description = "예약의 날짜, 인원, 상태를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "예약 정보 수정 실패")
    })
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
    @Operation(summary = "예약 삭제", description = "예약 ID로 예약을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "예약 삭제 실패"),
            @ApiResponse(responseCode = "404", description = "해당 예약 정보가 없습니다."),
    })
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
    @Operation(summary = "전체 유저 조회", description = "관리자가 전체 유저 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 목록 반환")
    @GetMapping("/users")
    public ResponseEntity<List<UserListResponseDTO>> getAllUser(){
        List<UserListResponseDTO> allUsers = hostService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    /**
     * 유저 상세 조회 ( 관리자용 )
     * */
    @Operation(summary = "유저 상세 조회", description = "유저 ID로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 정보 반환")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailResponseDTO> getDetailUser(@PathVariable int userId){
        UserDetailResponseDTO responseDTO = hostService.getUserDetail(userId);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 관리자 권한으로 유저 정보 삭제
     * */
    @Operation(summary = "유저 삭제", description = "관리자 권한으로 유저를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "유저 삭제 실패")
    })
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
