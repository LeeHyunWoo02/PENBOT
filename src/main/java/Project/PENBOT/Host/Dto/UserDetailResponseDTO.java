package Project.PENBOT.Host.Dto;

import Project.PENBOT.User.Entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDetailResponseDTO {

    private String name;
    private String email;
    private String phone;
    private Role role;

}
