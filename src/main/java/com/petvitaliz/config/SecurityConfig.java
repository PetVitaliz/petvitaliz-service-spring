package com.petvitaliz.config;

import com.petvitaliz.security.JwtAuthenticationFilter;
import com.petvitaliz.security.JwtService;
import com.petvitaliz.security.RestAccessDeniedHandler;
import com.petvitaliz.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwtService,
                                           RestAuthenticationEntryPoint entryPoint,
                                           RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health", "/error").permitAll()
                        // Paginas "nao logadas" e listagem publica de produtos
                        .requestMatchers(HttpMethod.GET,
                                "/", "/servicos", "/contato", "/servicos-de-emergencia",
                                "/cadastar/pet", "/listar/pet", "/editar/pet/*", "/delete/pet/*",
                                "/adm/listar/produtos").permitAll()
                        // Cadastro, logins e inicio do reset de senha
                        .requestMatchers(HttpMethod.POST,
                                "/user/cadastro", "/user/login", "/user/login/adm", "/user/login/funcionario",
                                "/user/login/esqueci-a-senha", "/user/login/esqueci-a-senha-confirmar").permitAll()
                        // Trocar a senha: so com o cookie token_reset
                        .requestMatchers(HttpMethod.PUT, "/user/login/alterar-senha").hasRole("RESET")
                        .requestMatchers("/adm/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/funcionario/**").hasRole("FUNCIONARIO")
                        .anyRequest().hasRole("USUARIO"))
                // Criado com "new" (e nao como @Component) para nao rodar duas vezes na cadeia de filtros.
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Mesmo custo (10) do bcrypt do Express; os hashes ja gravados no banco continuam validos.
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.frontend-url:}") String frontendUrl) {
        List<String> origens = new ArrayList<>(List.of(
                "http://localhost:4200",      // Angular
                "http://localhost:3000",
                "https://petvitaliz.vercel.app"));

        if (frontendUrl != null && !frontendUrl.isBlank()) {
            origens.add(frontendUrl.trim().replaceAll("/+$", ""));
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origens);
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
