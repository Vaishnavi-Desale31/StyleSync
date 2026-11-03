package com.project.stylesync_ai.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BODY = "{\"name\":\"Test\",\"price\":10.00}";

    // ---------- Not logged in ----------

    @Test
    void anonymousCannotListProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Logged in as a customer ----------

    @Test
    @WithMockUser(roles = "USER")
    void customerCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void customerCannotUpdateProduct() throws Exception {
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void customerCannotDeactivateProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void customerCannotReactivateProduct() throws Exception {
        mockMvc.perform(patch("/api/products/1/reactivate"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void customerCannotCreateCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void customerCannotUseAdminArea() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isForbidden());
    }

    // ---------- Allowed in (not blocked by security) ----------

    @Test
    @WithMockUser(roles = "USER")
    void customerCanReadActiveProducts() throws Exception {
        mockMvc.perform(get("/api/products/active"))
                .andExpect(status().is(not(anyOf(is(401), is(403)))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminIsNotBlockedFromCreatingProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().is(not(anyOf(is(401), is(403)))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminIsNotBlockedFromAdminArea() throws Exception {
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().is(not(anyOf(is(401), is(403)))));
    }
}
