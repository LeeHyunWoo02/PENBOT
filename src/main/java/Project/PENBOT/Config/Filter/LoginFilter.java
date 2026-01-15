package Project.PENBOT.Config.Filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Optional;


public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;

        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith("application/json")) {
            throw new AuthenticationServiceException("Unsupported content type: " + contentType);
        }

        final String username;
        final String password;

        try {
            // 2. JSON 파싱
            JsonNode root = objectMapper.readTree(request.getInputStream());

            username = Optional.ofNullable(root.path("username").asText(null))
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> new AuthenticationServiceException("Missing username"));

            password = Optional.ofNullable(root.path("password").asText(null))
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> new AuthenticationServiceException("Missing password"));

        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid login payload");
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);

        setDetails(request, authRequest);

        return authenticationManager.authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);

        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        // Jackson ObjectMapper를 이용해 JSON 문자열 생성 (Map 등을 사용해도 됨)
        String jsonResponse = objectMapper.writeValueAsString(java.util.Map.of(
                "message", "Login Success",
                "redirectUrl", "/admin/dashboard"
        ));

        response.getWriter().write(jsonResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}