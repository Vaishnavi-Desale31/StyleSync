package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {


    Optional<Cart> findByIdAndUser_Email(Long cartId, String email);
}
