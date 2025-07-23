package Project.PENBOT.Booking.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 예약 생성 API 요청시에는 headcount까지 확인
 * 예약 가능 날짜 확인 API 는 headcount 제외
 * */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private int headcount;
}
