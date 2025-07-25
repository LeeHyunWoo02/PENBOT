package Project.PENBOT.ChatAPI.Dto;

import lombok.Getter;

@Getter
public class QueryResponseDTO {
    private String result;

    public QueryResponseDTO(String answer) {
        this.result = answer;
    }
}
