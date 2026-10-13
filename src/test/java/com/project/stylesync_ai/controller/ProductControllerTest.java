package com.project.stylesync_ai.controller;


import com.project.stylesync_ai.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    //Test 1: Valid pagination
    @Test
    void shouldReturn200ForValidPagination() throws Exception{
        when(productService.getProductsWithPagination(any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(get("/api/products/page")
                .param("page","0")
                .param("size","10"))
                .andExpect(status().isOk());
    }

    //Test2: Negative page number
    @Test
    void shouldReturn400ForNegativePage() throws  Exception{
        mockMvc.perform(get("/api/products/page")
                .param("page", "-1")
                .param("size","10"))
                .andExpect(status().isBadRequest());
    }

    // Test 3: Page size above 100
    @Test
    void shouldReturn400ForPageSizeAbove100() throws Exception {
        mockMvc.perform(get("/api/products/page")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    //Test4: Valid ascending sorting
    @Test
    void shouldReturn200ForValidAscendingSort() throws Exception{
        when(productService.getProductsSorted("name","asc"))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/products/sort")
                .param("sortBy", "name")
                .param("direction","asc"))
                .andExpect(status().isOk());
    }

    //Test5: Valid descending sorting
    @Test
    void shouldReturn200ForValidDescendingSort() throws Exception{
        when(productService.getProductsSorted("price","desc"))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/products/sort")
                .param("sortBy","price")
                .param("direction", "desc"))
                .andExpect(status().isOk());
    }

    // Test 6: Invalid sorting field
    @Test
    void shouldReturn400ForInvalidSortField() throws Exception{
        mockMvc.perform(get("/api/products/sort")
                .param("sortBy","password")
                .param("direction","asc"))
                .andExpect(status().isBadRequest());
    }

    // Test 7: Invalid sorting direction
    @Test
    void shouldReturn400ForInvalidDirection() throws Exception{
        mockMvc.perform(get("/api/products/sort")
                .param("sortBy","name")
                .param("direction","random"))
                .andExpect(status().isBadRequest());
    }
}