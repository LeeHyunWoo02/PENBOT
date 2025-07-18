package Project.PENBOT.Config;

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
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                );

        /**
         * csrf 보호 해제
         * */
        http
                .csrf((csrf) -> csrf.disable());

//        /**
//         * OAuth2 로그인 로직
//         * */
//        http
//                .oauth2Login((oauth) -> oauth
//                        .userInfoEndpoint((userInfo) ->
//                                {userInfo.userService(customOauth2UserService);}
//                        )
//                        .successHandler(customOAuth2SuccessHandler)
//                        .failureHandler(customOAuth2FailureHandler)
//                );


//        /**
//         * Custom FormLogin Filter And Custom Logout Filter
//         * */
//        http.
//                addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
//
//

        /**
         * cors 관련 설정
         * */
        http
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration config = new CorsConfiguration();

                                config.setAllowedOrigins(Collections.singletonList("http://localhost:57584"));
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
