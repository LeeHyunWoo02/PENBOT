package Project.PENBOT.Host.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BlockDateRequestDTO {
    @Schema(description = "예약을 차단할 시작 날짜", example = "2025-08-15", required = true)
    private LocalDate startDate;

    @Schema(description = "예약을 차단할 종료 날짜", example = "2025-08-17", required = true)
    private LocalDate endDate;

    @Schema(description = "차단 사유 (예: 정기점검, 내부 행사 등)", example = "정기 점검")
    private String reason;
}
