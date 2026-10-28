package com.project.stylesync_ai.dto;

import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.entity.WishlistItem;

import java.time.LocalDateTime;

public record WishlistItemResponse(
        Long id,
        Long productId,
        String name,
        String description,
        Double price,
        String brand,
        String imageUrl,
        String size,
        String color,
        LocalDateTime createdAt
) {
    public static WishlistItemResponse from(WishlistItem item) {
        Product product = item.getProduct();

        return new WishlistItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getBrand(),
                product.getImageUrl(),
                product.getSize(),
                product.getColor(),
                item.getCreatedAt()
        );
    }
}
