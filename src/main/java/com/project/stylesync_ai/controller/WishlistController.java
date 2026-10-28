package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.WishlistItemResponse;
import com.project.stylesync_ai.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemResponse addProduct(
            @PathVariable Long productId,
            Authentication authentication) {

        return wishlistService.addProduct(
                authentication.getName(), productId);
    }

    @GetMapping
    public List<WishlistItemResponse> getWishlist(
            Authentication authentication) {

        return wishlistService.getWishlist(
                authentication.getName());
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProduct(
            @PathVariable Long productId,
            Authentication authentication) {

        wishlistService.removeProduct(
                authentication.getName(), productId);
    }
}
