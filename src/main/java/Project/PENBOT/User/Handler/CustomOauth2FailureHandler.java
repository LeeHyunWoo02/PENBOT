package Project.PENBOT.User.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOauth2FailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        if(response.isCommitted()) {
            return;
        }
        String errorMessage;
        int statusCode;
        if (exception instanceof OAuth2AuthenticationException){
            statusCode = HttpServletResponse.SC_UNAUTHORIZED; // 401 Unauthorized
            errorMessage = "OAuth2 인증 실패: " + exception.getMessage();
        } else if (exception instanceof BadCredentialsException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            errorMessage = "유효하지 않은 인증 정보입니다.";
        } else if (exception instanceof InsufficientAuthenticationException) {
            statusCode = HttpServletResponse.SC_FORBIDDEN;
            errorMessage = "인증이 필요합니다.";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            errorMessage = "내부 인증 서비스 오류입니다. 잠시 후 다시 시도해주세요.";
        } else {
            statusCode = HttpServletResponse.SC_UNAUTHORIZED;
            errorMessage = "알 수 없는 인증 오류가 발생했습니다.";
        }

        response.sendError(statusCode, errorMessage);
    }
}
