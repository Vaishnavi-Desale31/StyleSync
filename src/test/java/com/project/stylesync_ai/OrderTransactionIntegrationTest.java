package com.project.stylesync_ai;

import java.math.BigDecimal;
import com.project.stylesync_ai.entity.Address;
import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.AddressRepository;
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
import com.project.stylesync_ai.entity.Order;
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
    private AddressRepository addressRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Address saveAddress(User user) {
        Address address = new Address();
        address.setUser(user);
        address.setLabel("Home");
        address.setFullName("Rahul Sharma");
        address.setPhone("9876543210");
        address.setLine1("12 MG Road");
        address.setCity("Thane");
        address.setState("Maharashtra");
        address.setPostalCode("400601");
        address.setCountry("India");
        return addressRepository.saveAndFlush(address);
    }

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
                new BigDecimal("500.00"),
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

        Address address = saveAddress(savedUser);

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
                () -> orderService.createOrderFromCart(
                        cartId, testEmail, address.getId())
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

    @Test
    void successfulCheckout_shouldPersistOrderItemsAndClearCart(){

        String testEmail = "success-test-"  + UUID.randomUUID()
                + "@example.com";

        User user = userRepository .saveAndFlush(
                new User(
                        "Checkout Test User",
                        testEmail,
                        "encoded-test-password"
                )
        );

        Product product = productRepository.saveAndFlush(
                new Product(
                        "Checkout Test T-shirt",
                        "Product for successful checkout testing",
                        new BigDecimal("500.00"),
                        "TestBrand",
                        null,
                        "M",
                        "Black",
                        null
                )
        );

        Cart cart = new Cart();
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cart.addItem(cartItem);

        cart = cartRepository.saveAndFlush(cart);
        Long cartId = cart.getId();

        Address address = saveAddress(user);

        Order savedOrder = orderService.createOrderFromCart(
                cartId, testEmail, address.getId());

        assertNotNull(savedOrder.getId());
        assertEquals("PLACED", savedOrder.getStatus());
        assertEquals(0, new BigDecimal("1000.00").compareTo(savedOrder.getTotalAmount()));

        Long persistedItemCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_items WHERE order_id = ?",
                Long.class,
                savedOrder.getId()
        );

        assertEquals(1, persistedItemCount);

        String persistedProductName = jdbcTemplate.queryForObject(
                "SELECT product_name FROM order_items WHERE order_id = ?",
                String.class,
                savedOrder.getId()
        );

        assertEquals("Checkout Test T-shirt", persistedProductName);

        Integer remainingCartItems = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?",
                Integer.class,
                cartId
        );

        assertEquals(0, remainingCartItems);
    }

    @Test
    void orderKeepsDeliveryAddressSnapshotAfterSavedAddressIsEdited() {

        String testEmail = "snapshot-test-" + UUID.randomUUID()
                + "@example.com";

        User user = userRepository.saveAndFlush(
                new User("Rahul Sharma", testEmail, "encoded-test-password"));

        Product product = productRepository.saveAndFlush(
                new Product(
                        "Snapshot Test T-shirt",
                        "Product for address snapshot testing",
                        new BigDecimal("500.00"),
                        "TestBrand",
                        null,
                        "M",
                        "Black",
                        null
                )
        );

        Cart cart = new Cart();
        cart.setUser(user);
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cart.addItem(cartItem);
        cart = cartRepository.saveAndFlush(cart);

        Address address = saveAddress(user);

        Order order = orderService.createOrderFromCart(
                cart.getId(), testEmail, address.getId());

        // The customer edits the saved address after ordering.
        address.setCity("Pune");
        address.setPostalCode("411001");
        addressRepository.saveAndFlush(address);

        String city = jdbcTemplate.queryForObject(
                "SELECT ship_city FROM orders WHERE id = ?",
                String.class, order.getId());
        String postalCode = jdbcTemplate.queryForObject(
                "SELECT ship_postal_code FROM orders WHERE id = ?",
                String.class, order.getId());

        assertEquals("Thane", city);
        assertEquals("400601", postalCode);
    }
}
