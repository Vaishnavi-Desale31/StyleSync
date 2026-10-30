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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.project.stylesync_ai.entity.Order;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getOrders_shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void getOrders_shouldReturn401WhenTokenIsInvalid() throws Exception {
        when(jwtConfig.extractUsername("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(jwtConfig).extractUsername("invalid-token");
        verifyNoInteractions(orderService);
    }

    @Test
    void getOrders_shouldNotCallServiceWhenTokenValidationFails() throws Exception {
        String email = "user@example.com";
        String token = "invalid-signature-token";

        UserDetails userDetails = User.withUsername(email)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(jwtConfig.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);
        when(jwtConfig.isTokenValid(token, userDetails)).thenReturn(false);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(jwtConfig).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(email);
        verify(jwtConfig).isTokenValid(token, userDetails);
        verify(orderService, never()).getAllOrders(email);
    }


    @Test
    void createCodOrder_shouldUseAuthenticatedUsersEmail() throws Exception {
        String email = "user@example.com";
        String token = "valid-cod-token";

        UserDetails userDetails = User.withUsername(email)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        Order order = new Order();
        order.setPaymentMethod("COD");
        order.setPaymentStatus("PENDING");
        order.setStatus("PLACED");

        when(jwtConfig.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);
        when(jwtConfig.isTokenValid(token, userDetails)).thenReturn(true);
        when(orderService.createCodOrderFromCart(3L, email))
                .thenReturn(order);

        mockMvc.perform(post("/api/orders/cod/3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        verify(orderService).createCodOrderFromCart(3L, email);
    }

    @Test
    void buyNow_shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/orders/buy-now")
                        .contentType("application/json")
                        .content("{\"productId\":1,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void buyNow_shouldUseAuthenticatedUsersEmail() throws Exception {
        String email = "user@example.com";
        String token = "valid-buy-now-token";

        UserDetails userDetails = User.withUsername(email)
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(jwtConfig.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);
        when(jwtConfig.isTokenValid(token, userDetails)).thenReturn(true);
        when(orderService.createCodOrderForProduct(5L, 2, email))
                .thenReturn(new Order());

        mockMvc.perform(post("/api/orders/buy-now")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"productId\":5,\"quantity\":2}"))
                .andExpect(status().isCreated());

        verify(orderService).createCodOrderForProduct(5L, 2, email);
    }

}