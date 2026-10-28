package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.WishlistItemResponse;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.entity.WishlistItem;
import com.project.stylesync_ai.repository.ProductRepository;
import com.project.stylesync_ai.repository.UserRepository;
import com.project.stylesync_ai.repository.WishlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistItemRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private User user;
    private Product product;
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        user = new User("Test User", email, "encoded-password");
        user.setId(1L);

        product = new Product(
                "Premium Black T-Shirt",
                "Premium cotton T-shirt for everyday wear",
                799.0,
                "StyleSync Basics",
                "/images/black-tshirt.jpeg",
                "L",
                "Black",
                null
        );

        // Product currently has no public ID setter.
        ReflectionTestUtils.setField(product, "id", 2L);

        // Lenient: some tests look up a different user (or a missing one),
        // so this shared stub is not used by every test.
        lenient().when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
    }

    @Test
    void addProduct_shouldSaveProductToWishlist() {
        when(wishlistRepository.existsByUser_IdAndProduct_Id(1L, 2L))
                .thenReturn(false);

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(wishlistRepository.save(any(WishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WishlistItemResponse response =
                wishlistService.addProduct(email, 2L);

        assertEquals(2L, response.productId());
        assertEquals("Premium Black T-Shirt", response.name());
        assertEquals(799.0, response.price());
        assertEquals("StyleSync Basics", response.brand());

        verify(wishlistRepository).save(any(WishlistItem.class));
    }

    @Test
    void addProduct_shouldRejectDuplicateProduct() {
        when(wishlistRepository.existsByUser_IdAndProduct_Id(1L, 2L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> wishlistService.addProduct(email, 2L)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(
                "Product is already in your wishlist",
                exception.getReason()
        );

        verify(wishlistRepository, never()).save(any());
        verify(productRepository, never()).findById(any());
    }

    @Test
    void addProduct_shouldReturnNotFoundForMissingProduct() {
        when(wishlistRepository.existsByUser_IdAndProduct_Id(1L, 99L))
                .thenReturn(false);

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> wishlistService.addProduct(email, 99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals("Product not found", exception.getReason());

        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void getWishlist_shouldReturnSavedProducts() {
        WishlistItem item = new WishlistItem(user, product);

        when(wishlistRepository
                .findAllByUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(item));

        List<WishlistItemResponse> result =
                wishlistService.getWishlist(email);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).productId());
        assertEquals("Premium Black T-Shirt", result.get(0).name());

        verify(wishlistRepository)
                .findAllByUser_IdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getWishlist_shouldReturnEmptyListWhenNoItemsExist() {
        when(wishlistRepository
                .findAllByUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<WishlistItemResponse> result =
                wishlistService.getWishlist(email);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getWishlist_shouldUseAuthenticatedUsersId() {
        User anotherUser = new User(
                "Another User",
                "another@example.com",
                "encoded-password"
        );
        anotherUser.setId(2L);

        when(userRepository.findByEmail("another@example.com"))
                .thenReturn(Optional.of(anotherUser));

        when(wishlistRepository
                .findAllByUser_IdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of());

        List<WishlistItemResponse> result =
                wishlistService.getWishlist("another@example.com");

        assertTrue(result.isEmpty());

        verify(wishlistRepository)
                .findAllByUser_IdOrderByCreatedAtDesc(2L);

        verify(wishlistRepository, never())
                .findAllByUser_IdOrderByCreatedAtDesc(1L);
    }

    @Test
    void removeProduct_shouldDeleteSavedProduct() {
        WishlistItem item = new WishlistItem(user, product);

        when(wishlistRepository.findByUser_IdAndProduct_Id(1L, 2L))
                .thenReturn(Optional.of(item));

        assertDoesNotThrow(
                () -> wishlistService.removeProduct(email, 2L)
        );

        verify(wishlistRepository).delete(item);
    }

    @Test
    void removeProduct_shouldReturnNotFoundWhenProductIsNotSaved() {
        when(wishlistRepository.findByUser_IdAndProduct_Id(1L, 2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> wishlistService.removeProduct(email, 2L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Product not found in your wishlist",
                exception.getReason()
        );

        verify(wishlistRepository, never()).delete(any());
    }

    @Test
    void addProduct_shouldReturnNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> wishlistService.addProduct(
                        "missing@example.com", 2L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals("User not found", exception.getReason());

        verify(wishlistRepository, never()).save(any());
    }
}