package Project.PENBOT.Host.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BlockedDateResponseDTO {
    private boolean success;
    private String message;
    private int blockedDateId;

    public BlockedDateResponseDTO (boolean success, String message, int blockedDateId) {
        this.success = success;
        this.message = message;
        this.blockedDateId = blockedDateId;
    }
}
