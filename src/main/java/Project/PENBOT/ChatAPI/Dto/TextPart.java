package Project.PENBOT.ChatAPI.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TextPart implements Part{
    private String text;
}
