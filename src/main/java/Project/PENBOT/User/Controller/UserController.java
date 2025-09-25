package Project.PENBOT.User.Controller;

import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.User.Dto.*;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Service.JoinService;
import Project.PENBOT.User.Service.UserService;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 API", description = "유저 정보 조회 및 수정 기능 제공")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final JoinService joinService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(JoinService joinService, UserService userService, JwtUtil jwtUtil) {
        this.joinService = joinService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }


    @Operation(summary = "유저 정보 수정", description = "이름, 전화번호 등의 유저 정보를 수정하고, 갱신된 JWT 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보 수정 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @PostMapping("/update")
    private ResponseEntity<JoinResponseDTO> UpdateUser(@RequestBody JoinUserReuqestDTO requestDTO,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String auth){
        User user = joinService.UpdateUser(requestDTO, auth);
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        String newToken = jwtUtil.createAccessToken(userId, String.valueOf(user.getRole()));
        JoinResponseDTO responseDTO = new JoinResponseDTO(true, "User updated successfully",
                newToken);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "유저 정보 조회", description = "유저 정보를 조회.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보 수정 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @GetMapping("/search")
    private ResponseEntity<UserSearchResponseDTO> SearchUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        UserSearchResponseDTO responseDTO = userService.searchUser(auth);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "유저 탈퇴", description = "유저 탈퇴")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보 수정 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @DeleteMapping("/delete")
    private ResponseEntity<UserResponseDTO> DeleteUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        UserResponseDTO responseDTO = userService.deleteUser(auth);
        return ResponseEntity.ok(responseDTO);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new UserResponseDTO(false,ex.getMessage()));
    }
}
