
package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.AddToCartRequest;
import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.service.CartService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cart createCart(Authentication authentication) {
        return cartService.createCart(authentication.getName());
    }

    @PostMapping("/{cartId}/items")
    public Cart addProduct(
            @PathVariable Long cartId,
            @RequestBody AddToCartRequest request,
            Authentication authentication) {

        return cartService.addProduct(
                cartId,
                request.getProductId(),
                request.getQuantity(),
                authentication.getName()
        );
    }

    @GetMapping("/{cartId}")
    public Cart getCart(@PathVariable Long cartId,
                        Authentication authentication) {
        return cartService.getCartById(cartId,
                authentication.getName());
    }

    @PutMapping("/{cartId}/items/{productId}")
    public Cart updateQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        return cartService.updateQuantity(
                cartId,
                productId,
                quantity,
                authentication.getName()
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public Cart removeProduct(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            Authentication authentication) {

        return cartService.removeProduct(
                cartId,
                productId,
                authentication.getName());
    }

    @DeleteMapping("/{cartId}/items")
    public Cart clearCart(
            @PathVariable Long cartId,
            Authentication authentication){
        return cartService.clearCart(
                cartId,
                authentication.getName());
    }
}
