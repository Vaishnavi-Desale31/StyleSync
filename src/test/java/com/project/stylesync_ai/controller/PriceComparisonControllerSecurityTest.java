package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.config.JwtConfig;
import com.project.stylesync_ai.config.SecurityConfig;
import com.project.stylesync_ai.security.JwtAuthenticationFilter;
import com.project.stylesync_ai.service.PriceComparisonService;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PriceComparisonController.class)
@Import(SecurityConfig.class)
class PriceComparisonControllerSecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private PriceComparisonService priceComparisonService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        // A plain mock filter swallows the request. Make it continue the chain instead.
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void createOffer_shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/price-comparison/2/offers").with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(priceComparisonService);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void createOffer_shouldReturn403ForNormalUser() throws Exception {
        mockMvc.perform(post("/api/price-comparison/2/offers").with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(priceComparisonService);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void updateOffer_shouldReturn403ForNormalUser() throws Exception {
        mockMvc.perform(put("/api/price-comparison/offers/1").with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(priceComparisonService);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = "ROLE_USER")
    void deleteOffer_shouldReturn403ForNormalUser() throws Exception {
        mockMvc.perform(delete("/api/price-comparison/offers/1").with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(priceComparisonService);
    }
}