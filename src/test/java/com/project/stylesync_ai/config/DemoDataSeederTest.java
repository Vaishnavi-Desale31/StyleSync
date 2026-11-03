package com.project.stylesync_ai.config;

import com.project.stylesync_ai.entity.Category;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.repository.CategoryRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DemoDataSeeder seeder;

    @Test
    void run_shouldCreateFourCategoriesWithTenProductsEach() {
        when(productRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findByNameIgnoreCase(any()))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        seeder.run();

        verify(categoryRepository, times(4)).save(any(Category.class));

        ArgumentCaptor<Product> captor =
                ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(40)).save(captor.capture());

        for (String name : List.of("Men", "Women", "Kids", "Others")) {
            long count = captor.getAllValues().stream()
                    .filter(product -> product.getCategory() != null
                            && name.equals(product.getCategory().getName()))
                    .count();
            assertEquals(10, count, name + " should have 10 products");
        }

        assertTrue(captor.getAllValues().stream()
                .allMatch(product -> product.getPrice() != null
                        && product.getPrice().compareTo(BigDecimal.ZERO) > 0));
    }

    @Test
    void run_shouldSkipProductsThatAlreadyExist() {
        Product existing = new Product();
        existing.setName("classic white oxford shirt");

        when(productRepository.findAll()).thenReturn(List.of(existing));
        when(categoryRepository.findByNameIgnoreCase(any()))
                .thenReturn(Optional.of(new Category("Existing")));

        seeder.run();

        verify(categoryRepository, never()).save(any());
        verify(productRepository, times(39)).save(any(Product.class));
    }
}