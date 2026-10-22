package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.pricecomparison.PriceComparisonService;
import com.project.stylesync_ai.pricecomparison.PriceOffer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.project.stylesync_ai.security.JwtAuthenticationFilter;

@WebMvcTest(
        controllers = PriceComparisonController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class PriceComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceComparisonService priceComparisonService;


    @Test
    void comparePrices_shouldReturnBadRequestForBlankProductName()
            throws Exception {

        mockMvc.perform(get("/api/price-comparison/test")
                        .param("productName", ""))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void comparePrices_shouldReturnBadRequestForWhitespaceProductName()
            throws Exception {

        mockMvc.perform(get("/api/price-comparison/test")
                        .param("productName", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comparePrices_shouldReturnOffersForValidProductName()
            throws Exception {

        when(priceComparisonService.comparePrices(anyList()))
                .thenReturn(List.of(
                        new PriceOffer(
                                "Blue Jeans",
                                "Flipkart",
                                new BigDecimal("699.00"),
                                "url"
                        )
                ));

        mockMvc.perform(get("/api/price-comparison/test")
                        .param("productName", "Blue Jeans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName")
                        .value("Blue Jeans"))
                .andExpect(jsonPath("$[0].platform")
                        .value("Flipkart"))
                .andExpect(jsonPath("$[0].price")
                        .value(699.00));
    }

    @Test
    void comparePrices_shouldReturnBadRequestWhenProductNameIsMissing()
            throws Exception {

        mockMvc.perform(get("/api/price-comparison/test"))
                .andExpect(status().isBadRequest());
    }
}