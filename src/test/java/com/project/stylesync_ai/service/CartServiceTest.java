package com.project.stylesync_ai.service;

import com.project.stylesync_ai.model.Cart;
import com.project.stylesync_ai.model.CartItem;
import com.project.stylesync_ai.model.Product;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void addProduct_shouldAddProductToCartSuccessfully() {
        Cart cart = new Cart();

        Product product = mock(Product.class);


        when(cartRepository.findById(2L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addProduct(2L, 2L, 1);

        assertEquals(1, result.getItems().size());
        assertSame(product, result.getItems().get(0).getProduct());
        assertEquals(1, result.getItems().get(0).getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void clearCart_shouldRemoveAllItems(){
        Cart cart = new Cart();

        CartItem item1 = new CartItem();
        item1.setQuantity(2);

        CartItem item2 = new CartItem();
        item2.setQuantity(1);

        cart.addItem(item1);
        cart.addItem(item2);

        assertEquals(2, cart.getItems().size());

        when(cartRepository.findById(2L))
                .thenReturn(Optional.of(cart));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.clearCart(2L);

        assertTrue(result.getItems().isEmpty());

        assertNull(item1.getCart());
        assertNull(item2.getCart());

        verify(cartRepository).save(cart);
    }
}
