package com.project.stylesync_ai.exception;

import com.project.stylesync_ai.config.JwtConfig;
import com.project.stylesync_ai.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerMvcTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerMvcTest.TestController.class})
class GlobalExceptionHandlerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @RestController
    static class TestController {

        @GetMapping("/test/illegal-argument")
        String illegalArgument() {
            throw new IllegalArgumentException("Invalid quantity");
        }

        @GetMapping("/test/not-found")
        String notFound() {
            throw new ResourceNotFoundException("Product not found");
        }

        @GetMapping("/test/unexpected-error")
        String unexpectedError() {
            throw new RuntimeException("Sensitive internal details");
        }
    }

    @Test
    void shouldReturn400ForIllegalArgument() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForMissingResource() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnSafe500ForUnexpectedError() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString(
                                        "Sensitive internal details"
                                )
                        )));
    }
}