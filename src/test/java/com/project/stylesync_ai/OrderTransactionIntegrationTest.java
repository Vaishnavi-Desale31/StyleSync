package com.project.stylesync_ai;

import com.project.stylesync_ai.model.Cart;
import com.project.stylesync_ai.model.CartItem;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.OrderRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import com.project.stylesync_ai.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
public class OrderTransactionIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private CartRepository cartRepositorySpy;

    @Test
    void checkoutFailure_shouldRollbackOrderAndPreserveCart(){

        //1. Save the product and retrives its persisted database entity.
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

        Product saveProduct = productRepository.saveAndFlush(productToSave);

        org.junit.jupiter.api.Assertions.assertNotNull(saveProduct.getId());

        Product product = productRepository.findById(saveProduct.getId())
                .orElseThrow(()-> new AssertionError("Saved product was not found"));

        //2. create a cart containing two units of the product
        Cart cart = new Cart();

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart.addItem(cartItem);

        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        //3. Record the database state before checkout.
        long orderBefore = orderRepository.count();

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

        //4. Force the cart save to fail after the order is saved
        doThrow(new IllegalStateException("Simulate cart save failure"))
                .when(cartRepositorySpy)
                .save(any(Cart.class));

        //5.checkout must throw an exception
        assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrderFromCart(cartId)
        );

        //6.Verify that the order was rolled back.
        assertEquals(
                orderBefore,
                orderRepository.count(),
                "A failed checkout must be leave an order in the database"
        );

        //7. Verify that the original cart item still exists
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
                "The cart items must remain after transaction rollback"
        );

        assertEquals(
                2,
                quantityAfter,
                "The original cart quantity must be preserved"
        );
    }
}
