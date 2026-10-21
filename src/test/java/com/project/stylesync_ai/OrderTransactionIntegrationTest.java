package com.project.stylesync_ai;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import com.project.stylesync_ai.repository.UserRepository;
import com.project.stylesync_ai.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class OrderTransactionIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private CartRepository cartRepositorySpy;

    @Test
    void checkoutFailure_shouldRollbackOrderAndPreserveCart() {

        // 1. Create a unique user for this integration test.
        String testEmail = "rollback-test-" + UUID.randomUUID()
                + "@example.com";

        User userToSave = new User(
                "Rollback Test User",
                testEmail,
                "encoded-test-password"
        );

        User savedUser = userRepository.saveAndFlush(userToSave);
        assertNotNull(savedUser.getId());

        // 2. Save the product and retrieve its persisted entity.
        Product productToSave = new Product(
                "Rollback Test T-shirt",
                "Product for transaction integration testing",
                500.0,
                "TestBrand",
                null,
                "M",
                "Black",
                null
        );

        Product savedProduct = productRepository.saveAndFlush(productToSave);
        assertNotNull(savedProduct.getId());

        Product product = productRepository.findById(savedProduct.getId())
                .orElseThrow(() ->
                        new AssertionError("Saved product was not found"));

        // 3. Create a cart owned by the test user.
        Cart cart = new Cart();
        cart.setUser(savedUser);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart.addItem(cartItem);

        cart = cartRepository.saveAndFlush(cart);
        Long cartId = cart.getId();

        // 4. Record the database state before checkout.
        long ordersBefore = orderRepository.count();

        Integer itemsBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?",
                Integer.class,
                cartId
        );

        Integer quantityBefore = jdbcTemplate.queryForObject(
                "SELECT quantity FROM cart_items WHERE cart_id = ?",
                Integer.class,
                cartId
        );

        assertEquals(1, itemsBefore);
        assertEquals(2, quantityBefore);

        // 5. Force the cart save to fail during checkout.
        doThrow(new IllegalStateException("Simulate cart save failure"))
                .when(cartRepositorySpy)
                .save(any(Cart.class));

        // 6. Checkout should throw an exception.
        assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrderFromCart(cartId, testEmail)
        );

        // 7. Verify that the order was rolled back.
        assertEquals(
                ordersBefore,
                orderRepository.count(),
                "A failed checkout must not leave an order in the database"
        );

        // 8. Verify that the original cart items remain unchanged.
        Integer itemsAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?",
                Integer.class,
                cartId
        );

        Integer quantityAfter = jdbcTemplate.queryForObject(
                "SELECT quantity FROM cart_items WHERE cart_id = ?",
                Integer.class,
                cartId
        );

        assertEquals(
                1,
                itemsAfter,
                "Cart items must remain after transaction rollback"
        );

        assertEquals(
                2,
                quantityAfter,
                "The original cart quantity must be preserved"
        );
    }
}
