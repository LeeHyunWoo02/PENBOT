package Project.PENBOT.ChatAPI.Dto;

import Project.PENBOT.ChatAPI.Entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Role role;
    private String message;
}
