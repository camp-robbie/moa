package com.sparta.moa.common.config;

import com.sparta.moa.common.dto.ApiResponse;
import com.sparta.moa.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html",
                                "/*.js", "/*.css", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/members/signup", "/api/members/login").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/posts", "/api/posts/*", "/api/posts/*/comments").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, e) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");

                            // 20회차에 만든 ApiResponse를 그대로 직렬화해서
                            // 응답 모양을 다른 실패와 똑같게 맞춥니다
                            objectMapper.writeValue(
                                    response.getWriter(),
                                    ApiResponse.error(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"));
                        }))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}