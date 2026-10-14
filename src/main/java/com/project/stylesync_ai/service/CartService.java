package com.project.stylesync_ai.service;

import com.project.stylesync_ai.model.Cart;
import com.project.stylesync_ai.model.CartItem;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart createCart() {
        return cartRepository.save(new Cart());
    }

    @Transactional
    public Cart addProduct(Long cartId, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        Cart cart = getCartById(cartId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return cartRepository.save(cart);
            }
        }

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        cart.addItem(cartItem);

        return cartRepository.save(cart);
    }


    @Transactional(readOnly = true)
    public Cart getCartById(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                ));
    }

    @Transactional
    public Cart updateQuantity(
            Long cartId, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        Cart cart = getCartById(cartId);

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                return cartRepository.save(cart);
            }
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found in cart"
        );
    }

    @Transactional
    public Cart removeProduct(Long cartId, Long productId) {

        Cart cart = getCartById(cartId);

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item ->
                        item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found in cart"
                ));

        cart.removeItem(itemToRemove);

        return cartRepository.save(cart);
    }

}
