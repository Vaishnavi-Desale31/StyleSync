package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    Optional<Order> findByIdAndUser_Email(Long aLong, String email);

    List<Order> findAllByUser_Email(String email);
}
