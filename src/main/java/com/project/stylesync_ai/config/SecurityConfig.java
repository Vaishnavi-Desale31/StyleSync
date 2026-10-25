
package com.project.stylesync_ai.config;

import com.project.stylesync_ai.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/api/auth/**"
                        ).permitAll()
                        .anyRequest().authenticated())

                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                (request, response, exception) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Unauthorized")))
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
