package Project.PENBOT.User.Dto;

import Project.PENBOT.User.Entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinUserReuqestDTO {
    private String pasword;
    private String email;
    private Role role;
}
