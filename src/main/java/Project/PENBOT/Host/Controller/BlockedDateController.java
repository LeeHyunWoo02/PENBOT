package Project.PENBOT.Host.Controller;

import Project.PENBOT.CustomException.BlockedDateConflictException;

import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.Host.Dto.BlockDateRequestDTO;
import Project.PENBOT.Host.Dto.BlockedDateResponseDTO;
import Project.PENBOT.Host.Dto.UnavailableDateDTO;
import Project.PENBOT.Host.Service.HostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "예약 차단 날짜 API", description = "관리자가 예약 불가 날짜를 생성, 조회, 삭제할 수 있는 API")
@RestController
@RequestMapping("/api/host")
public class BlockedDateController {

    private final HostService hostService;

    public BlockedDateController(HostService hostService) {
        this.hostService = hostService;
    }

    /**
     * 관리자가 막고자 하는 날짜 생성
     * */
    @Operation(summary = "예약 차단 날짜 등록", description = "관리자가 예약을 차단하고자 하는 날짜를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "예약 차단 날짜 생성 성공"),
            @ApiResponse(responseCode = "400", description = "이미 예약된 날짜와 겹쳐 생성 실패")
    })
    @PostMapping("/blocks")
    public ResponseEntity<BlockedDateResponseDTO> createdBlockedDate(@RequestBody BlockDateRequestDTO requestDTO){
        BlockedDateResponseDTO responseDTO = hostService.createBlockedDate(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * 관리자가 차단한 날짜 모두 조회
     * */
    @Operation(summary = "관리자가 차단한 날짜 조회", description = "관리자가 차단한 날짜를 모두 반환합니다.")
    @ApiResponse(responseCode = "200", description = "관리자가 차단한 날짜 목록 조회 성공")
    @GetMapping("/blocks")
    public ResponseEntity<List<UnavailableDateDTO>> getBlockedDates(){
        List<UnavailableDateDTO> responseDTO = hostService.getHostBlockedDates();
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 지금까지 예약 불가능한 날짜 모두 조회
     * */
    @Operation(summary = "예약 불가능한 날짜 전체 조회", description = "현재까지 예약되었거나 차단된 모든 날짜 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "예약 불가 날짜 목록 조회 성공")
    @GetMapping("/unavailable-dates")
    public ResponseEntity<List<UnavailableDateDTO>> getUnavailableDates(){
        List<UnavailableDateDTO> responseDTO = hostService.getUnavailableDates();
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 관리자가 blocked 한 날짜 삭제
     * */
    @Operation(summary = "예약 차단 날짜 삭제", description = "관리자가 등록한 예약 차단 날짜를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차단 날짜 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "차단 날짜 ID를 찾을 수 없음")
    })
    @DeleteMapping("/blocks/{blockedDateId}")
    public ResponseEntity<BlockedDateResponseDTO> deleteBlocked(@PathVariable int blockedDateId){
        BlockedDateResponseDTO responseDTO = hostService.deleteBlocked(blockedDateId);
        return ResponseEntity.ok(responseDTO);
    }

    @ExceptionHandler(BlockedDateConflictException.class)
    public ResponseEntity<BlockedDateResponseDTO> handleBlockedDateConflict(BlockedDateConflictException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new BlockedDateResponseDTO(false, ex.getMessage(), 0));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<BlockedDateResponseDTO> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BlockedDateResponseDTO(false, ex.getMessage(), 0));
    }
}
