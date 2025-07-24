package Project.PENBOT.ChatAPI.Dto;

import Project.PENBOT.ChatAPI.Entity.Sender;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Sender sender;
    private String message;
}
