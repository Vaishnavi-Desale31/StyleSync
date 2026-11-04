package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.PasswordResetToken;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.PasswordResetTokenRepository;
import com.project.stylesync_ai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {

    private static final Logger log =
            LoggerFactory.getLogger(PasswordResetService.class);

    static final long TOKEN_VALID_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Always completes silently, whether or not the email exists, so the
     * endpoint cannot be used to discover registered addresses.
     * Until an email service is connected, the reset link is written to
     * the application log.
     */
    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        User user = userRepository
                .findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElse(null);

        if (user == null) {
            return;
        }

        tokenRepository.deleteByUser_Id(user.getId());

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        tokenRepository.save(new PasswordResetToken(
                user,
                hash(token),
                LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES)));

        log.info("Password reset link for {} (valid {} minutes): "
                        + "{}/resetpassword.html?token={}",
                user.getEmail(), TOKEN_VALID_MINUTES, baseUrl, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw invalidToken();
        }

        if (newPassword == null || newPassword.length() < 8
                || newPassword.length() > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain 8 to 72 characters");
        }

        PasswordResetToken saved = tokenRepository
                .findByTokenHash(hash(token.trim()))
                .orElseThrow(this::invalidToken);

        if (saved.isUsed()
                || saved.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw invalidToken();
        }

        User user = saved.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        saved.setUsed(true);
        tokenRepository.save(saved);
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This reset link is invalid or has expired");
    }

    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}