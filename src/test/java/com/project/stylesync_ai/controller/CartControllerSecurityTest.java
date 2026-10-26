package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.config.JwtConfig;
import com.project.stylesync_ai.config.SecurityConfig;
import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CartController.class)
@Import(SecurityConfig.class)
class CartControllerSecurityTest {

    private static final String EMAIL = "user@example.com";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    // Dependencies of the real JwtAuthenticationFilter
    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getCart_shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cartService);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = "ROLE_USER")
    void getCart_shouldPassAuthenticatedUsersEmailToService() throws Exception {
        when(cartService.getCartById(1L, EMAIL)).thenReturn(new Cart());

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk());

        verify(cartService).getCartById(1L, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = "ROLE_USER")
    void getCart_shouldReturn404WhenCartDoesNotBelongToUser() throws Exception {
        when(cartService.getCartById(1L, EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isNotFound());

        verify(cartService).getCartById(1L, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = "ROLE_USER")
    void createCart_shouldPassAuthenticatedUsersEmailToService() throws Exception {
        when(cartService.createCart(EMAIL)).thenReturn(new Cart());

        mockMvc.perform(post("/api/cart"))
                .andExpect(status().isCreated());

        verify(cartService).createCart(EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = "ROLE_USER")
    void addProduct_shouldReturn404WhenCartIsNotOwnedByUser() throws Exception {
        when(cartService.addProduct(1L, 2L, 1, EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        mockMvc.perform(post("/api/cart/1/items")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": 2,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(cartService).addProduct(1L, 2L, 1, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = "ROLE_USER")
    void updateQuantity_shouldReturn404WhenCartIsNotOwnedByUser() throws Exception {
        when(cartService.updateQuantity(1L, 2L, 3, EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        mockMvc.perform(put("/api/cart/1/items/2").param("quantity", "3"))
                .andExpect(status().isNotFound());

        verify(cartService).updateQuantity(1L, 2L, 3, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = "ROLE_USER")
    void removeProduct_shouldReturn404WhenCartIsNotOwnedByUser() throws Exception {
        when(cartService.removeProduct(1L, 2L, EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        mockMvc.perform(delete("/api/cart/1/items/2"))
                .andExpect(status().isNotFound());

        verify(cartService).removeProduct(1L, 2L, EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = "ROLE_USER")
    void clearCart_shouldReturn404WhenCartIsNotOwnedByUser() throws Exception {
        when(cartService.clearCart(1L, EMAIL))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        mockMvc.perform(delete("/api/cart/1/items"))
                .andExpect(status().isNotFound());

        verify(cartService).clearCart(1L, EMAIL);
    }
}