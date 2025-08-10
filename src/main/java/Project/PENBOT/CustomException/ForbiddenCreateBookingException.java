package Project.PENBOT.CustomException;

public class ForbiddenCreateBookingException extends RuntimeException {
    public ForbiddenCreateBookingException() {
        super("예약을 생성할 수 없습니다. 이미 예약된 날짜가 있습니다.");
    }
    public ForbiddenCreateBookingException(String message) {
        super(message);
    }
}
