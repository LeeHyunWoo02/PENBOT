package Project.PENBOT.Host.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponseDTO {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String role; // 문자열로 반환
}
