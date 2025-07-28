package Project.PENBOT.Host.Dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnavailableDateDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason; // 예: "예약됨", "관리자 차단"
    private String type;   // "BOOKED" or "BLOCKED"
}
