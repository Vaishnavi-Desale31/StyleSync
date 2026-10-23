package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import com.project.stylesync_ai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String TEST_EMAIL = "vaishnavi@example.com";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addProduct_shouldRejectZeroQuantity() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addProduct(2L, 2L, 0, TEST_EMAIL)
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
                () -> cartService.addProduct(2L, 2L, -1, TEST_EMAIL)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals(
                "Quantity must be greater than zero",
                exception.getReason()
        );
    }

    @Test
    void addProduct_shouldThrowExceptionWhenCartNotFound(){

        when(cartRepository.findByIdAndUser_Email(999L, TEST_EMAIL))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.getCartById(999L, TEST_EMAIL)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());
    }

    @Test
    void addProduct_shouldThrowExceptionWhenProductNotFound(){

        when(cartRepository.findByIdAndUser_Email(2L,TEST_EMAIL))
                .thenReturn(Optional.of(new Cart()));

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addProduct(2L, 999L,1, TEST_EMAIL)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Product not found", exception.getReason());
    }

    @Test
    void addProduct_shouldAddProductToCartSuccessfully() {
        Cart cart = new Cart();
        Product product = mock(Product.class);


        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addProduct(2L, 2L, 1, TEST_EMAIL);

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

        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.clearCart(2L,TEST_EMAIL);

        assertTrue(result.getItems().isEmpty());

        assertNull(item1.getCart());
        assertNull(item2.getCart());

        verify(cartRepository).save(cart);
    }

    @Test
    void updateQuantity_shouldRejectZeroQuantity(){

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.updateQuantity(2L,2L,0,TEST_EMAIL)
        );

        assertEquals(400,exception.getStatusCode().value());
        assertEquals(
                "Quantity must be greater than zero",
                exception.getReason()
        );
    }

    @Test
    void createCart_shouldSaveAndReturnCartForUser(){
        //Arrange: return the same cart that is saved
        User user = new User(
               "Test User",
               TEST_EMAIL,
               "encoded=password"
        );

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(user));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        //Act:create a cart
        Cart result = cartService.createCart(TEST_EMAIL);

        //Assert:cart is returned and save
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertSame(user, result.getUser());

        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(cartRepository).save(result);
    }

    @Test
    void createCart_ShouldFailWhenUserDoesNotExist(){
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.createCart(TEST_EMAIL)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("User not found", exception.getReason());

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getCartById_shouldReturnCartWhenItExists(){
        //Arrange: prepare an existing cart
        Cart cart = new Cart();
        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));

        //Act: retrieve the cart
        Cart result = cartService.getCartById(2L, TEST_EMAIL);

        //Assert: the same cart is returned
        assertSame(cart, result);
        verify(cartRepository).findByIdAndUser_Email(2L,TEST_EMAIL);

    }

    @Test
    void addProduct_shouldIncrementQuantityWhenProductAlreadyExists(){
        //Arrange: cart already contains two units of this product
        Cart cart = new Cart();
        Product product = mock(Product.class);
        when(product.getId())
                .thenReturn(2L);

        CartItem existingItem = new CartItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(2);
        cart.addItem(existingItem);

        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act: add three more units
        Cart result = cartService.addProduct(2L,2L,3, TEST_EMAIL);

        //Assert: quantity becomes five, not two separate items
        assertEquals(1, result.getItems().size());
        assertEquals(5,result.getItems().get(0).getQuantity());

        verify(cartRepository).save(cart);
    }

    @Test
    void updateQuantity_shouldUpdateExistingProductQuantity(){
        //Arrange: add a product wih quantity two
        Cart cart = new Cart();
        Product product = mock(Product.class);
        when(product.getId())
                .thenReturn(2L);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        cart.addItem(item);

        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act : set quantity to four
        Cart result = cartService.updateQuantity(2L,2L,4, TEST_EMAIL);

        //Assert: quantity is replaced to four
        assertEquals(4, result.getItems().get(0).getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateQuantity_shouldFailWhenProductIsNotInCart() {
        // Arrange: cart exists but contains no items
        Cart cart = new Cart();
        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));

        // Act: try to update a product that is not in the cart
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.updateQuantity(2L, 99L, 3, TEST_EMAIL)
        );

        // Assert: service returns a not-found error
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Product not found in cart", exception.getReason());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeProduct_shouldRemoveExistingProduct() {
        // Arrange: cart contains one product
        Cart cart = new Cart();
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(2L);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        cart.addItem(item);

        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: remove the product
        Cart result = cartService.removeProduct(2L, 2L, TEST_EMAIL);

        // Assert: cart is empty and relationship is removed
        assertTrue(result.getItems().isEmpty());
        assertNull(item.getCart());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeProduct_shouldFailWhenProductIsNotInCart() {
        // Arrange: cart exists but is empty
        Cart cart = new Cart();
        when(cartRepository.findByIdAndUser_Email(2L, TEST_EMAIL))
                .thenReturn(Optional.of(cart));

        // Act: try to remove a missing product
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.removeProduct(2L, 99L, TEST_EMAIL)
        );

        // Assert: not-found error is returned
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Product not found in cart", exception.getReason());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void clearCart_shouldFailWhenCartDoesNotExist() {
        // Arrange: requested cart is missing
        when(cartRepository.findByIdAndUser_Email(99L, TEST_EMAIL))
                .thenReturn(Optional.empty());

        // Act: try to clear the missing cart
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.clearCart(99L,TEST_EMAIL)
        );

        // Assert: not-found error is returned
        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());
        verify(cartRepository, never()).save(any(Cart.class));
    }


    @Test
    void getCartById_shouldRejectCartOwnedByAnotherUser() {
        String anotherUserEmail = "another@example.com";

        // The cart ID does not match the requesting user's email.
        when(cartRepository.findByIdAndUser_Email(2L, anotherUserEmail))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.getCartById(2L, anotherUserEmail)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());

        verify(cartRepository)
                .findByIdAndUser_Email(2L, anotherUserEmail);
    }

    @Test
    void addProduct_shouldRejectCartOwnedByAnotherUser() {
        String anotherUserEmail = "another@example.com";

        when(cartRepository.findByIdAndUser_Email(2L, anotherUserEmail))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addProduct(
                        2L, 2L, 1, anotherUserEmail
                )
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());

        verify(cartRepository, never()).save(any(Cart.class));
        verifyNoInteractions(productRepository);
    }

    @Test
    void clearCart_shouldRejectCartOwnedByAnotherUser() {
        String anotherUserEmail = "another@example.com";

        when(cartRepository.findByIdAndUser_Email(2L, anotherUserEmail))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.clearCart(2L, anotherUserEmail)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());

        verify(cartRepository, never()).save(any(Cart.class));
    }


    @Test
    void updateQuantity_shouldRejectCartOwnedByAnotherUser() {
        String anotherUserEmail = "another@example.com";

        when(cartRepository.findByIdAndUser_Email(2L, anotherUserEmail))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.updateQuantity(
                        2L, 2L, 5, anotherUserEmail
                )
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeProduct_shouldRejectCartOwnedByAnotherUser() {
        String anotherUserEmail = "another@example.com";

        when(cartRepository.findByIdAndUser_Email(2L, anotherUserEmail))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.removeProduct(
                        2L, 2L, anotherUserEmail
                )
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Cart not found", exception.getReason());

        verify(cartRepository, never()).save(any(Cart.class));
    }
}
