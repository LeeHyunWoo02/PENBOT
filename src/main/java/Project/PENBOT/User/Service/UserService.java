package Project.PENBOT.User.Service;

import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.Booking.Entity.Booking;
import Project.PENBOT.CustomException.UserNotFoundException;
import Project.PENBOT.User.Converter.UserConverter;
import Project.PENBOT.User.Dto.UserResponseDTO;
import Project.PENBOT.User.Dto.UserSearchResponseDTO;
import Project.PENBOT.User.Entity.User;
import Project.PENBOT.User.Repository.UserRepository;
import Project.PENBOT.User.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 유저정보 조회
     * 예약 내역까지 조회
     * */
    public UserSearchResponseDTO searchUser(String auth){
        try{
            String token = auth.replace("Bearer ", "");
            Claims claims = jwtUtil.getClaims(token);
            int userId = claims.get("userId", Integer.class);
            User user = userRepository.findById(userId);
            Set<Booking> userBookings = user.getBookings();
            HashMap<String, BookingSimpleDTO> myBookings = new HashMap<>();
            for(Booking booking : userBookings){
                BookingSimpleDTO bookingSimpleDTO = BookingSimpleDTO.builder()
                        .bookingId(booking.getId())
                        .startDate(String.valueOf(booking.getStartDate()))
                        .endDate(String.valueOf(booking.getEndDate()))
                        .status(booking.getStatus())
                        .build();
                myBookings.put(String.valueOf(booking.getId()), bookingSimpleDTO);
            }

            return UserConverter.ToDTO(user,myBookings);
        } catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
    }

    public UserResponseDTO deleteUser(String auth){
        String token = auth.replace("Bearer ", "");
        Claims claims = jwtUtil.getClaims(token);
        int userId = claims.get("userId", Integer.class);
        try{
            userRepository.deleteById(userId);
            return new UserResponseDTO(true, "User deleted successfully");
        } catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
    }
}
