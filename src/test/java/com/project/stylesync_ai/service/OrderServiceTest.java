
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.model.Cart;
import com.project.stylesync_ai.model.CartItem;
import com.project.stylesync_ai.model.Order;
import com.project.stylesync_ai.model.OrderItem;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderFromCart_shouldCreateOrderSuccessfully() {
        // Arrange: prepare a cart with two T-shirts
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(2);

        when(product.getId()).thenReturn(2L);
        when(product.getName())
                .thenReturn("Premium Black T-Shirt");
        when(product.getPrice()).thenReturn(799.0);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: create the order
        Order result = orderService.createOrderFromCart(3L);

        // Assert: verify order details
        assertNotNull(result);
        assertEquals("PLACED", result.getStatus());
        assertEquals(1598.0, result.getTotalAmount(), 0.001);
        assertNotNull(result.getOrderDate());

        assertEquals(1, result.getItems().size());

        OrderItem item = result.getItems().get(0);
        assertEquals(2L, item.getProductId());
        assertEquals("Premium Black T-Shirt",
                item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(799.0, item.getPrice(), 0.001);
        assertSame(result, item.getOrder());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderFromCart_shouldFailWhenCartIsMissing() {
        when(cartRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrderFromCart(99L)
        );

        assertEquals(404, exception.getStatusCode().value());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderFromCart_shouldFailWhenCartIsEmpty() {
        Cart cart = mock(Cart.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));

        when(cart.getItems()).thenReturn(new ArrayList<>());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrderFromCart(3L)
        );

        assertEquals(400, exception.getStatusCode().value());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_shouldReturnOrderWhenItExists() {
        Order order = new Order();

        when(orderRepository.findById(3L))
                .thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(3L);

        assertSame(order, result);
        verify(orderRepository).findById(3L);
    }

    @Test
    void getOrderById_shouldFailWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(99L)
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void createOrderFromCart_shouldCalculateTotalForMultipleProducts(){
        Cart cart = mock(Cart.class);
        CartItem tshirtItem = mock(CartItem.class);
        CartItem jeansItem = mock(CartItem.class);

        Product tshirt = mock(Product.class);
        Product jeans = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems())
                .thenReturn(List.of(tshirtItem, jeansItem));

        when(tshirtItem.getProduct()).thenReturn(tshirt);
        when(tshirtItem.getQuantity()).thenReturn(2);
        when(tshirt.getId()).thenReturn(2L);
        when(tshirt.getName()).thenReturn("Premium Black T-Shirt");
        when(tshirt.getPrice()).thenReturn(799.0);

        when(jeansItem.getProduct()).thenReturn(jeans);
        when(jeansItem.getQuantity()).thenReturn(1);
        when(jeans.getId()).thenReturn(3L);
        when(jeans.getName()).thenReturn("Jeans");
        when(jeans.getPrice()).thenReturn(1299.0);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrderFromCart(3L);

        assertEquals(2897.0, result.getTotalAmount(), 0.001);
        assertEquals(2,result.getItems().size());

        verify(orderRepository).save(any(Order.class));
    }
}
