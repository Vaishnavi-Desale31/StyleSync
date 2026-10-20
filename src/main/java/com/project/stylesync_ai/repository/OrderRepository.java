package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
