package Project.PENBOT.CustomException;

public class UnableBookingException extends RuntimeException {
    public UnableBookingException() {
        super("이미 예약이 되어있어서 예약을 할 수 없습니다.");
    }
    public UnableBookingException(String message) {
        super(message);
    }
}
