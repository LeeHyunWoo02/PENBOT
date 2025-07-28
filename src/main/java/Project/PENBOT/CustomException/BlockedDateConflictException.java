package Project.PENBOT.CustomException;

public class BlockedDateConflictException extends RuntimeException {
    public BlockedDateConflictException(){
        super("이미 예약된 날짜가 있어 차단할 수 없습니다");
    }
    public BlockedDateConflictException(String message) {
        super(message);
    }
}
