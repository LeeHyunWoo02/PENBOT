package Project.PENBOT.User.Service;

import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.User.Converter.UserConverter;
import Project.PENBOT.User.Dto.UserSearchResponseDTO;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public UserSearchResponseDTO searchUser(String auth){
        try{
            String token = auth.replace("Bearer ", "");
            Claims claims = jwtUtil.getClaims(token);
            int userId = claims.get("userId", Integer.class);
            User user = userRepository.findById(userId);

            return UserConverter.ToDTO(user);
        } catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
    }
}
