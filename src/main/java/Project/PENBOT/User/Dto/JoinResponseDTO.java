package Project.PENBOT.User.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinResponseDTO {
    private final boolean success;
    private final String message;
    private final String token;
    public JoinResponseDTO(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }
}
