package Project.PENBOT.User.Controller;

import Project.PENBOT.User.Dto.JoinResponseDTO;
import Project.PENBOT.User.Dto.JoinUserReuqestDTO;
import Project.PENBOT.User.Dto.UserSearchRequestDTO;
import Project.PENBOT.User.Dto.UserSearchResponseDTO;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Service.JoinService;
import Project.PENBOT.User.Service.UserService;
import Project.PENBOT.User.Util.JwtUtil;
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
    private ResponseEntity<JoinResponseDTO> UpdateUser(@RequestBody JoinUserReuqestDTO requestDTO){
        User user = joinService.UpdateUser(requestDTO);
        String newToken = jwtUtil.createAccessToken(requestDTO.getEmail(), String.valueOf(user.getRole()));
        JoinResponseDTO responseDTO = new JoinResponseDTO(true, "User updated successfully",
                newToken);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/search")
    private ResponseEntity<UserSearchResponseDTO> SearchUser(@RequestBody UserSearchRequestDTO requestDTO) {
        UserSearchResponseDTO responseDTO = userService.searchUser(requestDTO.getEmail());
        return ResponseEntity.ok(responseDTO);
    }
}
