package Project.PENBOT.User.Converter;

import Project.PENBOT.Booking.Dto.BookingSimpleDTO;
import Project.PENBOT.User.Dto.JoinTempUserDTO;
import Project.PENBOT.User.Dto.UserSearchResponseDTO;
import Project.PENBOT.User.Entity.Role;
import Project.PENBOT.User.Entity.User;

import java.util.HashMap;

public class UserConverter {
    public static User toEntity(JoinTempUserDTO dto) {
        return User.builder()
                .name(dto.getName())
                .password(dto.getPassword())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .role(Role.valueOf(dto.getRole()))
                .provider(dto.getProvider())
                .providerId(dto.getProciderId())
                .build();
    }

    public static UserSearchResponseDTO ToDTO(User user, HashMap<String, BookingSimpleDTO> myBookings) {
        return UserSearchResponseDTO.builder()
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .myBookings(myBookings)
                .build();
    }
}
