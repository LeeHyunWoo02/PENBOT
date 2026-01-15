package Project.PENBOT.Config;

import Project.PENBOT.Config.Filter.LoginFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final ObjectMapper objectMapper;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, ObjectMapper objectMapper) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), objectMapper);

        // 프론트엔드에서 요청 보내는 URL과 일치시켜야 함 (예: /api/admin/login)
        loginFilter.setFilterProcessesUrl("/api/admin/login");

        return loginFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/host/**").hasRole("HOST")
                        .requestMatchers("/admin/**").hasRole("HOST")
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                );

        /**
         * csrf 보호 해제
         * */
        http
                .csrf((csrf) -> csrf.disable());

        http.formLogin((auth) -> auth.disable());
        http.httpBasic((auth) -> auth.disable());

        http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
