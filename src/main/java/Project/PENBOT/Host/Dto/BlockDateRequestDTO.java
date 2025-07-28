package Project.PENBOT.Host.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BlockDateRequestDTO {
    public LocalDate startDate;
    public LocalDate endDate;
    public String reason;
}
