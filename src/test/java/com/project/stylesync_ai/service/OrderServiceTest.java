package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Order;
import com.project.stylesync_ai.entity.OrderItem;
import com.project.stylesync_ai.entity.Product;
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

    // 1. Successful checkout with one product
    @Test
    void createOrderFromCart_shouldCreateOrderSuccessfully() {
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of(cartItem));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(2);

        when(product.getId()).thenReturn(2L);
        when(product.getName()).thenReturn("Premium Black T-Shirt");
        when(product.getPrice()).thenReturn(799.0);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrderFromCart(3L);

        assertNotNull(result);
        assertEquals("PLACED", result.getStatus());
        assertNotNull(result.getOrderDate());
        assertEquals(1598.0, result.getTotalAmount(), 0.001);

        assertEquals(1, result.getItems().size());

        OrderItem item = result.getItems().get(0);
        assertEquals(2L, item.getProductId());
        assertEquals("Premium Black T-Shirt", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(799.0, item.getPrice(), 0.001);
        assertSame(result, item.getOrder());

        verify(orderRepository).save(any(Order.class));
    }

    // 2. Checkout fails when the cart does not exist
    @Test
    void createOrderFromCart_shouldFailWhenCartIsMissing() {
        when(cartRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrderFromCart(99L)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());

        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // 3. Checkout fails when the cart is empty
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
        assertEquals(
                "Cannot create an order from an empty cart",
                exception.getReason()
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(cart, never()).clearItems();
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // 4. Checkout fails when the cart items list is null
    @Test
    void createOrderFromCart_shouldRejectNullItems() {
        Cart cart = mock(Cart.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrderFromCart(3L)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals(
                "Cannot create an order from an empty cart",
                exception.getReason()
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // 5. Total is calculated correctly for multiple products
    @Test
    void createOrderFromCart_shouldCalculateTotalForMultipleProducts() {
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

        // (2 * 799) + (1 * 1299) = 2897
        assertEquals(2897.0, result.getTotalAmount(), 0.001);
        assertEquals(2, result.getItems().size());

        verify(orderRepository).save(any(Order.class));
    }

    // 6. Cart is cleared after successful checkout
    @Test
    void createOrderFromCart_shouldClearCartAfterSuccessfulOrder() {
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of(cartItem));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(1);

        when(product.getId()).thenReturn(2L);
        when(product.getName()).thenReturn("T-Shirt");
        when(product.getPrice()).thenReturn(799.0);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrderFromCart(3L);

        assertNotNull(result);
        verify(cart).clearItems();
        verify(cartRepository).save(cart);
    }

    // 7. Order is saved before the cart is cleared and saved
    @Test
    void createOrderFromCart_shouldSaveOrderBeforeClearingCart() {
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of(cartItem));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(1);

        when(product.getId()).thenReturn(2L);
        when(product.getName()).thenReturn("T-Shirt");
        when(product.getPrice()).thenReturn(799.0);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrderFromCart(3L);

        var order = inOrder(orderRepository, cart, cartRepository);

        order.verify(orderRepository).save(any(Order.class));
        order.verify(cart).clearItems();
        order.verify(cartRepository).save(cart);
    }

    // 8. Order retains the product price used at checkout
    @Test
    void createOrderFromCart_shouldStoreProductPriceSnapshot() {
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of(cartItem));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(2);

        when(product.getId()).thenReturn(2L);
        when(product.getName()).thenReturn("T-Shirt");
        when(product.getPrice()).thenReturn(799.0);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrderFromCart(3L);

        assertEquals(799.0, result.getItems().get(0).getPrice(), 0.001);
        assertEquals(1598.0, result.getTotalAmount(), 0.001);
    }

    // 9. Cart is not cleared when saving the order fails
    @Test
    void createOrderFromCart_shouldNotClearCartWhenOrderSaveFails() {
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of(cartItem));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(1);

        when(product.getId()).thenReturn(2L);
        when(product.getName()).thenReturn("T-Shirt");
        when(product.getPrice()).thenReturn(799.0);

        when(orderRepository.save(any(Order.class)))
                .thenThrow(new RuntimeException("Database save failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrderFromCart(3L)
        );

        assertEquals("Database save failed", exception.getMessage());

        verify(cart, never()).clearItems();
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // 10. Failure while saving the cleared cart is propagated
    @Test
    void createOrderFromCart_shouldPropagateCartSaveFailure() {
        Cart cart = mock(Cart.class);
        CartItem cartItem = mock(CartItem.class);
        Product product = mock(Product.class);

        when(cartRepository.findById(3L))
                .thenReturn(Optional.of(cart));
        when(cart.getItems()).thenReturn(List.of(cartItem));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(1);

        when(product.getId()).thenReturn(2L);
        when(product.getName()).thenReturn("T-Shirt");
        when(product.getPrice()).thenReturn(799.0);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(cartRepository.save(cart))
                .thenThrow(new RuntimeException("Cart save failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrderFromCart(3L)
        );

        assertEquals("Cart save failed", exception.getMessage());

        verify(orderRepository).save(any(Order.class));
        verify(cart).clearItems();
        verify(cartRepository).save(cart);
    }

    // 11. Existing order is returned by ID
    @Test
    void getOrderById_shouldReturnOrderWhenItExists() {
        Order expectedOrder = new Order();

        when(orderRepository.findById(3L))
                .thenReturn(Optional.of(expectedOrder));

        Order result = orderService.getOrderById(3L);

        assertSame(expectedOrder, result);
        verify(orderRepository).findById(3L);
    }

    // 12. Missing order returns a 404 exception
    @Test
    void getOrderById_shouldFailWhenOrderDoesNotExist() {
        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(99L)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Order not found", exception.getReason());
    }
}