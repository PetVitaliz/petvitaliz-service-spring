package com.petvitaliz.config;

import com.petvitaliz.controller.AuthController;
import com.petvitaliz.security.JwtAuthenticationFilter;
import com.petvitaliz.security.JwtService;
import com.petvitaliz.security.RestAccessDeniedHandler;
import com.petvitaliz.security.RestAuthenticationEntryPoint;
import com.petvitaliz.security.TokenType;
import com.petvitaliz.web.ClientIpResolver;
import com.petvitaliz.web.IdempotencyFilter;
import com.petvitaliz.web.IdempotencyStore;
import com.petvitaliz.web.RateLimitFilter;
import com.petvitaliz.web.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int BCRYPT_STRENGTH = 10;

    private static final String[] DOCUMENTATION_ROUTES = {
            "/docs", "/docs/**", "/docs-json", "/docs-json/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    private static final String[] PUBLIC_PAGES = {
            "/", "/servicos", "/contato", "/servicos-de-emergencia",
            "/cadastar/pet", "/listar/pet", "/editar/pet/*", "/delete/pet/*",
            "/adm/listar/produtos"
    };

    private static final String[] PUBLIC_AUTH_ROUTES = {
            "/user/cadastro", "/user/login", "/user/login/adm", "/user/login/funcionario",
            "/user/login/esqueci-a-senha", "/user/login/esqueci-a-senha-confirmar"
    };

    private static final List<String> DEFAULT_ORIGINS = List.of(
            "http://localhost:4200",
            "http://localhost:3000",
            "http://localhost:8081",
            "http://localhost:19006",
            "https://petvitaliz.vercel.app");

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwtService,
                                           RateLimiter rateLimiter,
                                           RateLimitProperties rateLimitProperties,
                                           IdempotencyStore idempotencyStore,
                                           IdempotencyProperties idempotencyProperties,
                                           ClientIpResolver clientIpResolver,
                                           RestAuthenticationEntryPoint entryPoint,
                                           RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health", "/error").permitAll()
                        .requestMatchers(DOCUMENTATION_ROUTES).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_PAGES).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_AUTH_ROUTES).permitAll()
                        .requestMatchers(HttpMethod.PUT, TokenType.PASSWORD_RESET_PATH)
                        .hasRole(TokenType.PASSWORD_RESET.getRole())
                        .requestMatchers("/adm", "/adm/**").hasRole(TokenType.ADMIN.getRole())
                        .requestMatchers("/funcionario", "/funcionario/**").hasRole(TokenType.EMPLOYEE.getRole())
                        .anyRequest().hasRole(TokenType.USER.getRole()))
                .addFilterAfter(new RateLimitFilter(rateLimiter, rateLimitProperties, clientIpResolver),
                        CorsFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new IdempotencyFilter(idempotencyStore, idempotencyProperties, clientIpResolver),
                        JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.frontend-url:}") String frontendUrls) {
        List<String> extraOrigins = Arrays.stream(frontendUrls.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .map(origin -> origin.replaceAll("/+$", ""))
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Stream.concat(DEFAULT_ORIGINS.stream(), extraOrigins.stream()).distinct().toList());
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION,
                IdempotencyFilter.KEY_HEADER, AuthController.CLIENT_TYPE_HEADER));
        config.setExposedHeaders(List.of(
                IdempotencyFilter.REPLAYED_HEADER, HttpHeaders.RETRY_AFTER,
                RateLimitFilter.LIMIT_HEADER, RateLimitFilter.REMAINING_HEADER));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
