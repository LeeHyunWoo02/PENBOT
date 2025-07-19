package Project.PENBOT.User.Converter;

import Project.PENBOT.User.Dto.JoinTempUserDTO;
import Project.PENBOT.User.Entity.Role;
import Project.PENBOT.User.Entity.User;

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
}
