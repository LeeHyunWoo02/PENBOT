package Project.PENBOT.CustomException;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException() {
        super("외부 API 호출에 실패했습니다.");
    }
    public ExternalApiException(String message) {
        super(message);
    }
}
