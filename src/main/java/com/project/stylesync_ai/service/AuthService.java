
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.config.JwtConfig;
import com.project.stylesync_ai.dto.AuthResponse;
import com.project.stylesync_ai.dto.LoginRequest;
import com.project.stylesync_ai.dto.RegisterRequest;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtConfig jwtConfig;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtConfig = jwtConfig;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered");
        }

        User user = new User(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()));

        userRepository.save(user);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        String token = jwtConfig.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                jwtConfig.getExpirationMs());
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.password()));

        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        String token = jwtConfig.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                jwtConfig.getExpirationMs());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
