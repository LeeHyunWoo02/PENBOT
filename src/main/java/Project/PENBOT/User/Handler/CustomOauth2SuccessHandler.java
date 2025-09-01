package Project.PENBOT.User.Handler;

import Project.PENBOT.User.Dto.CustomUserDetails;
import Project.PENBOT.User.Util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public CustomOauth2SuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
            throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        int loginId = userDetails.getId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String accessToken = jwtUtil.createAccessToken(loginId,role);


        // 프론트엔드 콜백 URL (환경에 따라 수정)
        String redirectUrl = "http://localhost:8080/oauth2/redirect?accessToken=" + accessToken;

        response.sendRedirect(redirectUrl);

    }
}
