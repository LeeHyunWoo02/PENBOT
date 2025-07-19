package Project.PENBOT.User.Service;

import Project.PENBOT.User.Converter.UserConverter;
import Project.PENBOT.User.Dto.JoinTempUserDTO;
import Project.PENBOT.User.Dto.JoinUserReuqestDTO;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.PasswordUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class JoinService {
    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    public JoinService(UserRepository userRepository, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.passwordUtil = passwordUtil;
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
        String password = dto.getPasword();

        User user = userRepository.findByEmail(email);
        if(user != null && passwordUtil.matchesPassword(password,user.getPassword())){
            user.setRole(dto.getRole());
            user.setPassword(passwordUtil.encodePassword(password));
        }
        return userRepository.save(user);
    }
}
