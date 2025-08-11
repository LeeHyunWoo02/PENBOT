package Project.PENBOT.Config;

import Project.PENBOT.User.Filter.JwtFilter;
import Project.PENBOT.User.Handler.CustomOauth2FailureHandler;
import Project.PENBOT.User.Handler.CustomOauth2SuccessHandler;
import Project.PENBOT.User.Service.CustomOauth2UserService;
import Project.PENBOT.User.Util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CustomOauth2UserService customOauth2UserService;
    private final JwtUtil jwtUtil;
    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
    private final CustomOauth2FailureHandler customOauth2FailureHandler;

    public SecurityConfig(CustomOauth2UserService customOauth2UserService, JwtUtil jwtUtil,
                          CustomOauth2SuccessHandler customOauth2SuccessHandler, CustomOauth2FailureHandler customOauth2FailureHandler) {
        this.customOauth2UserService = customOauth2UserService;
        this.jwtUtil = jwtUtil;
        this.customOauth2SuccessHandler = customOauth2SuccessHandler;
        this.customOauth2FailureHandler = customOauth2FailureHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/host/**").hasRole("HOST")
                        .requestMatchers("/api/bookings","/api/bookings/myall").hasRole("GUEST")
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                );

        /**
         * csrf 보호 해제
         * */
        http
                .csrf((csrf) -> csrf.disable());

        /**
         * OAuth2 로그인 로직
         * */
        http
                .oauth2Login((oauth) -> oauth
                        .userInfoEndpoint((userInfo) ->
                                {userInfo.userService(customOauth2UserService);}
                        )
                        .successHandler(customOauth2SuccessHandler)
                        .failureHandler(customOauth2FailureHandler)
                );

        /**
         * jwt 필터 등록
         * */
        http.
                addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);



        /**
         * cors 관련 설정
         * */
        http
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration config = new CorsConfiguration();

                                config.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
                                config.setAllowedMethods(Collections.singletonList("*")); // 허용할 메소드 Get ect on
                                config.setAllowCredentials(true);
                                config.setAllowedHeaders(Collections.singletonList("*"));
                                config.setMaxAge(3600L);

                                config.setExposedHeaders(Collections.singletonList("Authorization"));

                                return config;
                            }
                        }));

        return http.build();
    }
}
