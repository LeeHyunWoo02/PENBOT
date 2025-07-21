package Project.PENBOT.User.Service;

import Project.PENBOT.User.Converter.UserConverter;
import Project.PENBOT.User.Dto.JoinTempUserDTO;
import Project.PENBOT.User.Dto.JoinUserReuqestDTO;
import Project.PENBOT.User.Entity.Role;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public User JoinTempUser(JoinTempUserDTO dto){
        String email = dto.getEmail();

        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email already in use");
        }

        User user = UserConverter.toEntity(dto);
        return userRepository.save(user);
    }


    @Transactional
    public User UpdateUser(JoinUserReuqestDTO dto, String auth){
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        String password = dto.getPassword();

        User user = userRepository.findById(userId);
        try{
            if(user != null){
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(Role.GUEST);
            }
        } catch (NullPointerException e){
            throw new IllegalArgumentException("User not found or password mismatch");
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid password format");
        }
        return userRepository.save(user);
    }
}
