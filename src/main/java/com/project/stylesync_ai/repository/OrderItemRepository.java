package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
