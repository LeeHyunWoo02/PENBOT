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
        String loginId = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String accessToken = jwtUtil.createAccessToken(loginId,role);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("message","Authentication successful.");
        response.getWriter().write(new ObjectMapper().writeValueAsString(tokens));
        response.getWriter().flush();
        response.getWriter().close();

    }
}
