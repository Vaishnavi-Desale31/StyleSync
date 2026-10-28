package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, Long> {

    boolean existsByUser_IdAndProduct_Id(
            Long userId, Long productId);

    List<WishlistItem> findAllByUser_IdOrderByCreatedAtDesc(
            Long userId);

    Optional<WishlistItem> findByUser_IdAndProduct_Id(
            Long userId, Long productId);
}
