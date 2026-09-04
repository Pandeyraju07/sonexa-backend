package com.sonexa.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonexa.backend.common.ApiResponse;
import com.sonexa.backend.constant.ErrorCode;
import com.sonexa.backend.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;
    private final ObjectMapper objectMapper;

    @Value("${sonexa.cors.allowed-origins:https://zynera.app,https://www.zynera.app,https://sonexa.app,https://www.sonexa.app}")
    private String allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            Environment environment,
            ObjectMapper objectMapper
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        ApiResponse<Object> body = ResponseUtil.failure(
                                ErrorCode.UNAUTHORIZED, "Authentication required", null);
                        objectMapper.writeValue(response.getWriter(), body);
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        ApiResponse<Object> body = ResponseUtil.failure(
                                ErrorCode.FORBIDDEN, "Access denied", null);
                        objectMapper.writeValue(response.getWriter(), body);
                    })
            )
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                                "/",
                                "/api",
                                "/api/v1",
                                "/api/v1/",
                                "/health",
                                "/status",
                                "/api/v1/health/**",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/admin/login",
                                "/api/v1/auth/send-otp",
                                "/api/v1/auth/verify-otp",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/auth/google",
                                "/api/v1/auth/apple",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()
                        .requestMatchers("/media/**").permitAll()
                        .requestMatchers("/api/v1/config/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/music/home",
                                "/api/v1/music/trending",
                                "/api/v1/music/search",
                                "/api/v1/music/tracks/**",
                                "/api/v1/music/albums/**",
                                "/api/v1/music/playlists/**",
                                "/api/v1/music/artists/**",
                                "/api/v1/music/genres",
                                "/api/v1/music/moods",
                                "/api/v1/music/lyrics",
                                "/api/v1/podcasts",
                                "/api/v1/podcasts/**",
                                "/api/v1/live-events/**",
                                "/api/v1/ipop/**",
                                "/api/v1/search/categories",
                                "/api/v1/home"
                        ).permitAll();

                if (!prod) {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                }

                auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Request-Id"));
        configuration.setExposedHeaders(List.of("X-Request-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
