
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.RazorpayVerifyRequest;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RazorpayPaymentServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String KEY_ID = "rzp_test_key";
    private static final String KEY_SECRET = "test_secret";
    private static final String RAZORPAY_ORDER_ID = "order_test123";
    private static final String PAYMENT_ID = "pay_test123";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private RazorpayPaymentService razorpayPaymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                razorpayPaymentService, "keyId", KEY_ID);
        ReflectionTestUtils.setField(
                razorpayPaymentService, "keySecret", KEY_SECRET);
    }

    @Test
    void verifyPayment_shouldRejectNullRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(null, EMAIL)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Payment verification fields are required",
                exception.getReason()
        );
    }

    @Test
    void verifyPayment_shouldRejectMissingFieldsBeforeCheckingCredentials() {
        RazorpayVerifyRequest request = new RazorpayVerifyRequest();

        ReflectionTestUtils.setField(
                razorpayPaymentService, "keyId", "");
        ReflectionTestUtils.setField(
                razorpayPaymentService, "keySecret", "");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(request, EMAIL)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Payment verification fields are required",
                exception.getReason()
        );
        verify(orderRepository, never())
                .findByIdAndUser_Email(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void verifyPayment_shouldRejectUnconfiguredCredentials() {
        ReflectionTestUtils.setField(
                razorpayPaymentService, "keyId", "");
        ReflectionTestUtils.setField(
                razorpayPaymentService, "keySecret", "");

        RazorpayVerifyRequest request = validRequest();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(request, EMAIL)
        );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatusCode()
        );
        assertEquals(
                "Razorpay credentials are not configured",
                exception.getReason()
        );
        verify(orderRepository, never())
                .findByIdAndUser_Email(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void verifyPayment_shouldReturnNotFoundForUnknownOrder() {
        RazorpayVerifyRequest request = validRequest();

        when(orderRepository.findByIdAndUser_Email(10L, EMAIL))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(request, EMAIL)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Order not found", exception.getReason());
    }

    @Test
    void verifyPayment_shouldRejectMismatchedRazorpayOrderId() {
        RazorpayVerifyRequest request = validRequest();

        Order order = new Order();
        order.setPaymentMethod("RAZORPAY");
        order.setRazorpayOrderId("order_different");

        when(orderRepository.findByIdAndUser_Email(10L, EMAIL))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(request, EMAIL)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Razorpay order does not match",
                exception.getReason()
        );
    }

    @Test
    void verifyPayment_shouldRejectCodOrder() {
        RazorpayVerifyRequest request = validRequest();

        Order order = new Order();
        order.setPaymentMethod("COD");
        order.setRazorpayOrderId(RAZORPAY_ORDER_ID);

        when(orderRepository.findByIdAndUser_Email(10L, EMAIL))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(request, EMAIL)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Razorpay order does not match",
                exception.getReason()
        );
    }


    @Test
    void verifyPayment_shouldRejectInvalidSignature() {
        RazorpayVerifyRequest request = validRequest();
        request.setRazorpaySignature("definitely-not-a-valid-hex-signature");

        Order order = razorpayOrder();

        when(orderRepository.findByIdAndUser_Email(10L, EMAIL))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(request, EMAIL)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid payment signature", exception.getReason());
    }


    @Test
    void verifyPayment_shouldReturnAlreadyPaidOrderForSamePaymentId() {
        RazorpayVerifyRequest request = validRequest();
        request.setRazorpaySignature(
                generateSignature(RAZORPAY_ORDER_ID, PAYMENT_ID));

        Order order = razorpayOrder();
        order.setPaymentStatus("PAID");
        order.setRazorpayPaymentId(PAYMENT_ID);

        when(orderRepository.findByIdAndUser_Email(10L, EMAIL))
                .thenReturn(Optional.of(order));

        Order result =
                razorpayPaymentService.verifyPayment(request, EMAIL);

        assertSame(order, result);
        verify(orderRepository, never()).save(order);
        verify(cartRepository, never())
                .findByIdAndUser_Email(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void verifyPayment_shouldRejectDifferentPaymentIdForAlreadyPaidOrder() {
        RazorpayVerifyRequest request = validRequest();
        request.setRazorpayPaymentId("pay_different");
        request.setRazorpaySignature(
                generateSignature(
                        RAZORPAY_ORDER_ID, "pay_different"));

        Order order = razorpayOrder();
        order.setPaymentStatus("PAID");
        order.setRazorpayPaymentId(PAYMENT_ID);

        when(orderRepository.findByIdAndUser_Email(10L, EMAIL))
                .thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> razorpayPaymentService.verifyPayment(request, EMAIL)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "Order has already been paid",
                exception.getReason()
        );
    }

    private RazorpayVerifyRequest validRequest() {
        RazorpayVerifyRequest request = new RazorpayVerifyRequest();
        request.setOrderId(10L);
        request.setRazorpayOrderId(RAZORPAY_ORDER_ID);
        request.setRazorpayPaymentId(PAYMENT_ID);
        request.setRazorpaySignature(
                generateSignature(RAZORPAY_ORDER_ID, PAYMENT_ID));
        return request;
    }

    private Order razorpayOrder() {
        Order order = new Order();
        order.setPaymentMethod("RAZORPAY");
        order.setRazorpayOrderId(RAZORPAY_ORDER_ID);
        order.setPaymentStatus("PENDING");
        order.setTotalAmount(799.00);
        order.setCartId(2L);
        return order;
    }

    private String generateSignature(
            String razorpayOrderId, String paymentId) {

        try {
            String payload = razorpayOrderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    KEY_SECRET.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");

            mac.init(secretKey);
            byte[] hash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte value : hash) {
                hex.append(String.format("%02x", value & 0xff));
            }

            return hex.toString();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not generate test signature", exception);
        }
    }
}
