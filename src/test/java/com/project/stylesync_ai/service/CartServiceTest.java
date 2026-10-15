package com.project.stylesync_ai.service;

import com.project.stylesync_ai.model.Cart;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.spel.ast.OpInc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addProduct_shouldRejectZeroQuantity() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addProduct(2L, 2L, 0)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals(
                "Quantity must be greater than zero",
                exception.getReason()
        );
    }

    @Test
    void addProduct_shouldRejectNegativeQuantity(){

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addProduct(2L, 2L, -1)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals(
                "Quantity must be greater than zero",
                exception.getReason()
        );
    }

    @Test
    void addProduct_shouldThrowExceptionWhenCartNotFound(){

        when(cartRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.getCartById(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());
    }

    @Test
    void addProduct_shouldThrowExceptionWhenProductNotFound(){

        when(cartRepository.findById(2L))
                .thenReturn(Optional.of(new Cart()));

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addProduct(2L, 999L,1)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Product not found", exception.getReason());
    }

}
