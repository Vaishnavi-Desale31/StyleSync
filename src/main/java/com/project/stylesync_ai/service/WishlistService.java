
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.WishlistItemResponse;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.entity.WishlistItem;
import com.project.stylesync_ai.repository.ProductRepository;
import com.project.stylesync_ai.repository.UserRepository;
import com.project.stylesync_ai.repository.WishlistItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(
            WishlistItemRepository wishlistRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public WishlistItemResponse addProduct(
            String email, Long productId) {

        User user = getUser(email);

        if (wishlistRepository.existsByUser_IdAndProduct_Id(
                user.getId(), productId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is already in your wishlist");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        WishlistItem item =
                wishlistRepository.save(new WishlistItem(user, product));

        return WishlistItemResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getWishlist(String email) {
        User user = getUser(email);

        return wishlistRepository
                .findAllByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(WishlistItemResponse::from)
                .toList();
    }

    @Transactional
    public void removeProduct(String email, Long productId) {
        User user = getUser(email);

        WishlistItem item = wishlistRepository
                .findByUser_IdAndProduct_Id(user.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found in your wishlist"));

        wishlistRepository.delete(item);
    }
}
