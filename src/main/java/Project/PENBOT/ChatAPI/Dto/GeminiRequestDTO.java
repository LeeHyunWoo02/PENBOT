package Project.PENBOT.ChatAPI.Dto;

import Project.PENBOT.ChatAPI.Entity.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class GeminiRequestDTO {
    private List<Content> contents;

    public GeminiRequestDTO(Role role, String text){
        this.contents = List.of(new Content(role,List.of(new TextPart(text))));
    }
}
