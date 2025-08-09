package Project.PENBOT.Host.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailableDateDTO {

    private int blockedDateId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason; // 예: "예약됨", "관리자 차단"
    private String type;   // "BOOKED" or "BLOCKED"
}
