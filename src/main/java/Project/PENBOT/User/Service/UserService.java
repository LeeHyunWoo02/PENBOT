package Project.PENBOT.User.Service;

import Project.PENBOT.User.Converter.UserConverter;
import Project.PENBOT.User.Dto.UserSearchResponseDTO;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserSearchResponseDTO searchUser(String email){
        try{
            User user = userRepository.findByEmail(email);

            return UserConverter.ToDTO(user);
        } catch (NullPointerException e){
            throw new NullPointerException("User not found with email: " + email);
        }
    }
}
