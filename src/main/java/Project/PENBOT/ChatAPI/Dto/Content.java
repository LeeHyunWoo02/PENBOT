package Project.PENBOT.ChatAPI.Dto;

import Project.PENBOT.ChatAPI.Entity.ChatRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private ChatRole chatRole;
    private List<TextPart> parts;
}
