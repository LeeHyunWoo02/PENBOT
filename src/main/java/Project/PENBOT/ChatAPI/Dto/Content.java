package Project.PENBOT.ChatAPI.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Content {
    private List<TextPart> parts;
}
