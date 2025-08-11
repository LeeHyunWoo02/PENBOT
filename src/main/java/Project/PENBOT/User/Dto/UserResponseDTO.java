package Project.PENBOT.User.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private boolean success;
    private String message;

    public UserResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
