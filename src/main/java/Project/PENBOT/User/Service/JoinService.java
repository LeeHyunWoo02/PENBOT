package Project.PENBOT.User.Service;

import Project.PENBOT.User.Converter.UserConverter;
import Project.PENBOT.User.Dto.JoinTempUserDTO;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    private final UserRepository userRepository;

    public JoinService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User JoinUser(JoinTempUserDTO dto){
        String email = dto.getEmail();

        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email already in use");
        }

        User user = UserConverter.toEntity(dto);
        return userRepository.save(user);
    }
}
