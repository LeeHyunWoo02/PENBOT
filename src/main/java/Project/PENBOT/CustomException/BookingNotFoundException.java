package Project.PENBOT.CustomException;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException() {
        super("존재하지 않는 예약입니다.");
    }
    public BookingNotFoundException(String message) {
        super(message);
    }
}
