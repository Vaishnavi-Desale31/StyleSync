
package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search products by name
    List<Product> findByNameContainingIgnoreCase(String name);

    // Filter products by category ID
    List<Product> findByCategoryId(Long categoryId);
}
