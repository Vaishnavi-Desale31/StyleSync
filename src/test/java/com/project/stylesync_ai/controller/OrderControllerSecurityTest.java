package com.project.stylesync_ai.controller;
import com.project.stylesync_ai.config.JwtConfig;
import com.project.stylesync_ai.config.SecurityConfig;
import com.project.stylesync_ai.security.JwtAuthenticationFilter;
import com.project.stylesync_ai.service.OrderService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService  orderService;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getOrders_shouldReturn401WhenTokenIsMissing()throws Exception{
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrders_shouldReturn401WhenTokenIsInvalid()throws Exception{
        when(jwtConfig.extractUsername("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}
