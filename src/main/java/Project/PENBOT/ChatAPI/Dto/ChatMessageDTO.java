package Project.PENBOT.ChatAPI.Dto;

import Project.PENBOT.ChatAPI.Entity.ChatRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private ChatRole role;
    private String message;
}
