package com.project.stylesync_ai.security;

import com.project.stylesync_ai.config.JwtConfig;
import com.project.stylesync_ai.config.SecurityConfig;
import com.project.stylesync_ai.controller.OrderController;
import com.project.stylesync_ai.service.OrderService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtConfig.class
})
@TestPropertySource(properties = {
        "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "jwt.expiration-ms=86400000"
})
class JwtAuthenticationIntegrationTest {

    private static final String EMAIL = "jwt-test@example.com";

    private static final String TEST_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtConfig jwtConfig;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UserDetails testUser() {
        return User.withUsername(EMAIL)
                .password("test-password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void protectedEndpoint_shouldReturn401WhenTokenIsMissing()
            throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userDetailsService, orderService);
    }

    @Test
    void protectedEndpoint_shouldReturn401WhenTokenIsMalformed()
            throws Exception {

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void protectedEndpoint_shouldReturn401WhenSignatureIsInvalid()
            throws Exception {

        String otherSecret =
                "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXowMTIzNDU=";

        String token = Jwts.builder()
                .subject(EMAIL)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(
                        Decoders.BASE64.decode(otherSecret)))
                .compact();

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void protectedEndpoint_shouldReturn401WhenTokenIsExpired()
            throws Exception {

        long now = System.currentTimeMillis();

        String expiredToken = Jwts.builder()
                .subject(EMAIL)
                .issuedAt(new Date(now - 20_000))
                .expiration(new Date(now - 10_000))
                .signWith(Keys.hmacShaKeyFor(
                        Decoders.BASE64.decode(TEST_SECRET)))
                .compact();

        mockMvc.perform(get("/api/orders")
                        .header("Authorization",
                                "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void protectedEndpoint_shouldAcceptValidToken() throws Exception {

        UserDetails user = testUser();
        String token = jwtConfig.generateToken(user);

        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(user);

        when(orderService.getAllOrders(EMAIL))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(userDetailsService).loadUserByUsername(EMAIL);
        verify(orderService).getAllOrders(EMAIL);
    }
}