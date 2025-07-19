package Project.PENBOT.User.Controller;

import Project.PENBOT.User.Dto.JoinResponseDTO;
import Project.PENBOT.User.Dto.JoinUserReuqestDTO;
import Project.PENBOT.User.Service.JoinService;
import Project.PENBOT.User.Util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final JoinService joinService;
    private final JwtUtil jwtUtil;
    public UserController(JoinService joinService, JwtUtil jwtUtil) {
        this.joinService = joinService;
        this.jwtUtil = jwtUtil;
    }

    private ResponseEntity<JoinResponseDTO> UpdateUser(@RequestBody JoinUserReuqestDTO requestDTO){
        joinService.UpdateUser(requestDTO);
        String newToken = jwtUtil.createAccessToken(requestDTO.getEmail(), String.valueOf(requestDTO.getRole()));
        JoinResponseDTO responseDTO = new JoinResponseDTO(true, "User updated successfully",
                newToken);
        return ResponseEntity.ok(responseDTO);
    }
}
