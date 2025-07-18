package Project.PENBOT.User.Dto;

import Project.PENBOT.User.Entity.Role;
import lombok.Getter;


@Getter
public class JoinUserDTO {

    private String name;
    private String password;
    private String phone;
    private String email;
    private String role;
    private String provider;
    private String prociderId;

    public JoinUserDTO(String name, String password, String phone,
                       String email, String role, String provider, String prociderId) {
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.prociderId = prociderId;
    }

}
