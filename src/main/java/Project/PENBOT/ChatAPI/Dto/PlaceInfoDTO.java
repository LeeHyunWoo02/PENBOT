package Project.PENBOT.ChatAPI.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlaceInfoDTO {
    private String name;     // 장소명
    private double rating;   // 평점
    private String address;  // 주소
}
