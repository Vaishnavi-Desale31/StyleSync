package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search products by name
    List<Product> findByNameContainingIgnoreCase(String name);

    // Filter products by category ID
    List<Product> findByCategoryId(Long categoryId);

    // Get only active products
    List<Product> findByActiveTrue();

    // Search only active products
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    // Filter active products by category ID
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByActiveTrue(Sort sort);
}
