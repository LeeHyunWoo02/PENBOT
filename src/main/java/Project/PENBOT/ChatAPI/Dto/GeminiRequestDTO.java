package Project.PENBOT.ChatAPI.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class GeminiRequestDTO {
    private List<Content> contents;

    public GeminiRequestDTO(String text){
        this.contents = List.of(new Content(List.of(new TextPart(text))));
    }
}
