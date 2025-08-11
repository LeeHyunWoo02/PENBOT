package Project.PENBOT.ChatAPI.Dto;

import Project.PENBOT.ChatAPI.Entity.ChatRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class GeminiRequestDTO {
    private List<Content> contents;

    public GeminiRequestDTO(ChatRole chatRole, String text){
        this.contents = List.of(new Content(chatRole,List.of(new TextPart(text))));
    }
}
