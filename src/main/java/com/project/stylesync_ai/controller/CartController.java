
package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.AddToCartRequest;
import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.service.CartService;

import org.springframework.http.HttpStatus;
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
    public Cart createCart() {
        return cartService.createCart();
    }

    @PostMapping("/{cartId}/items")
    public Cart addProduct(
            @PathVariable Long cartId,
            @RequestBody AddToCartRequest request) {

        return cartService.addProduct(
                cartId,
                request.getProductId(),
                request.getQuantity()
        );
    }

    @GetMapping("/{cartId}")
    public Cart getCart(@PathVariable Long cartId) {
        return cartService.getCartById(cartId);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public Cart updateQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return cartService.updateQuantity(
                cartId, productId, quantity
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public Cart removeProduct(
            @PathVariable Long cartId,
            @PathVariable Long productId) {

        return cartService.removeProduct(cartId, productId);
    }

    @DeleteMapping("/{cartId}/items")
    public Cart clearCart(@PathVariable Long cartId){
        return cartService.clearCart(cartId);
    }
}
