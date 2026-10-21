package com.project.stylesync_ai.service;

import com.project.stylesync_ai.entity.Cart;
import com.project.stylesync_ai.entity.CartItem;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.User;
import com.project.stylesync_ai.repository.CartRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import com.project.stylesync_ai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Cart createCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Cart cart = new Cart();
        cart.setUser(user);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart addProduct(
            Long cartId, Long productId, Integer quantity, String email) {

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        Cart cart = getCartById(cartId, email);

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
    public Cart getCartById(Long cartId, String email) {
        return cartRepository.findByIdAndUser_Email(cartId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                ));
    }

    @Transactional
    public Cart updateQuantity(
            Long cartId, Long productId, Integer quantity, String email) {

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        Cart cart = getCartById(cartId, email);

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
    public Cart removeProduct(
            Long cartId, Long productId, String email) {

        Cart cart = getCartById(cartId, email);

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

    @Transactional
    public Cart clearCart(Long cartId, String email) {
        Cart cart = getCartById(cartId, email);
        cart.clearItems();

        return cartRepository.save(cart);
    }
}