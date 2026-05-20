package com.certiva.api.Config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final HttpAuditFilter httpAuditFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter, HttpAuditFilter httpAuditFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.httpAuditFilter = httpAuditFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/usuarios/login", "/api/usuarios/registrar").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/roles/registro", "/api/tipo-documentos").permitAll()
                .requestMatchers("/api/certificados/verificar/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/usuarios/importar-csv").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/rol").hasRole("ADMIN")

                .requestMatchers("/api/roles/**").hasRole("ADMIN")

                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                .requestMatchers("/api/auditoria/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/check-in", "/api/inscripciones/confirmar-qr")
                    .hasAnyRole("ADMIN", "MONITOR")

                .requestMatchers(HttpMethod.GET, "/api/inscripciones/mis").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.PATCH, "/api/usuarios/mi-perfil/telefono").hasRole("ESTUDIANTE")
                .requestMatchers("/api/certificados/mis/**").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.GET, "/api/eventos/mi-panel").hasAnyRole("ADMIN", "PROFESOR")

                .requestMatchers(HttpMethod.GET, "/api/eventos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/eventos").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.PUT, "/api/eventos/**").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/**").hasAnyRole("ADMIN", "PROFESOR")

                .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "PROFESOR", "MONITOR")

                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(httpAuditFilter, JwtTokenFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "https://*.ngrok-free.app", "https://*.ngrok.io"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
