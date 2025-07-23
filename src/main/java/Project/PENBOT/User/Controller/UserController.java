package Project.PENBOT.User.Controller;

import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.User.Dto.*;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Service.JoinService;
import Project.PENBOT.User.Service.UserService;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/search")
    private ResponseEntity<UserSearchResponseDTO> SearchUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        UserSearchResponseDTO responseDTO = userService.searchUser(auth);
        return ResponseEntity.ok(responseDTO);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new UserResponseDTO(false,ex.getMessage()));
    }
}
