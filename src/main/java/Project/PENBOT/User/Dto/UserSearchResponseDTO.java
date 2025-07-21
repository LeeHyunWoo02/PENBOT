package Project.PENBOT.User.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSearchResponseDTO {

    private String name;
    private String phone;
    private String email;

}
