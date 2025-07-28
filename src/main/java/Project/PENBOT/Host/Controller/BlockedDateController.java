package Project.PENBOT.Host.Controller;

import Project.PENBOT.CustomException.BlockedDateConflictException;
import Project.PENBOT.CustomException.BookingNotFoundException;
import Project.PENBOT.Host.Dto.BlockDateRequestDTO;
import Project.PENBOT.Host.Dto.BlockedDateResponseDTO;
import Project.PENBOT.Host.Dto.UnavailableDateDTO;
import Project.PENBOT.Host.Service.HostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hosts")
public class BlockedDateController {

    private final HostService hostService;

    public BlockedDateController(HostService hostService) {
        this.hostService = hostService;
    }

    /**
     * 관리자가 막고자 하는 날짜 생성
     * */
    @PostMapping("/blocks")
    public ResponseEntity<BlockedDateResponseDTO> createdBlockedDate(@RequestBody BlockDateRequestDTO requestDTO){
        BlockedDateResponseDTO responseDTO = hostService.createBlockedDate(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
    
    /**
     * 지금까지 예약 불가능한 날짜 모두 조회
     * */
    @GetMapping("/unavailable-dates")
    public ResponseEntity<List<UnavailableDateDTO>> getUnavailableDates(){
        List<UnavailableDateDTO> responseDTO = hostService.getUnavailableDates();
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * 관리자가 blocked 한 날짜 삭제
     * */
    @DeleteMapping("/blocks/{blockedDateId}")
    public ResponseEntity<BlockedDateResponseDTO> deleteBlocked(@PathVariable int blockedDateId){
        BlockedDateResponseDTO responseDTO = hostService.deleteBlocked(blockedDateId);
        return ResponseEntity.ok(responseDTO);
    }

    @ExceptionHandler(BlockedDateConflictException.class)
    public ResponseEntity<BlockedDateResponseDTO> handleBlockedDateConflict(BlockedDateConflictException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new BlockedDateResponseDTO(false, ex.getMessage(), 0));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<BlockedDateResponseDTO> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BlockedDateResponseDTO(false, ex.getMessage(), 0));
    }
}
