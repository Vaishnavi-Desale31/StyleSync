
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.RazorpayVerifyRequest;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RazorpayPaymentServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private RazorpayPaymentService razorpayPaymentService;

    @Test
    void verifyPayment_shouldRejectMissingFieldsBeforeCheckingCredentials() {
        RazorpayVerifyRequest request = new RazorpayVerifyRequest();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(
                        request, "test@example.com")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Payment verification fields are required",
                exception.getReason()
        );
    }
}

