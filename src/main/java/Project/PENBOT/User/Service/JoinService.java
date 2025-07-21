package Project.PENBOT.User.Service;

import Project.PENBOT.User.Converter.UserConverter;
import Project.PENBOT.User.Dto.JoinTempUserDTO;
import Project.PENBOT.User.Dto.JoinUserReuqestDTO;
import Project.PENBOT.User.Entity.Role;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public User UpdateUser(JoinUserReuqestDTO dto){
        String email = dto.getEmail();
        String password = dto.getPassword();

        User user = userRepository.findByEmail(email);
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
