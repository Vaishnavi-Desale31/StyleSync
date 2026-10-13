
package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.ProductRequest;
import com.project.stylesync_ai.exception.ResourceNotFoundException;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.repository.CategoryRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldGetAllProducts() {
        Product product = new Product();

        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertSame(product, result.get(0));
        verify(productRepository).findAll();
    }

    @Test
    void shouldGetProductsWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        Page<Product> expectedPage = new PageImpl<>(List.of(product));

        when(productRepository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<Product> result =
                productService.getProductsWithPagination(pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(product, result.getContent().get(0));
        verify(productRepository).findAll(pageable);
    }

    @Test
    void shouldSortProductsInAscendingOrder() {
        Product product = new Product();

        when(productRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(product));

        List<Product> result =
                productService.getProductsSorted("name", "asc");

        assertEquals(1, result.size());

        ArgumentCaptor<Sort> captor =
                ArgumentCaptor.forClass(Sort.class);

        verify(productRepository).findAll(captor.capture());

        assertEquals(
                Sort.Direction.ASC,
                captor.getValue().getOrderFor("name").getDirection()
        );
    }

    @Test
    void shouldSortProductsInDescendingOrder() {
        when(productRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

        productService.getProductsSorted("price", "desc");

        ArgumentCaptor<Sort> captor =
                ArgumentCaptor.forClass(Sort.class);

        verify(productRepository).findAll(captor.capture());

        assertEquals(
                Sort.Direction.DESC,
                captor.getValue().getOrderFor("price").getDirection()
        );
    }

    @Test
    void shouldGetProductById() {
        Long productId = 1L;
        Product product = new Product();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(productId);

        assertSame(product, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        Long productId = 99L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        verify(productRepository).findById(productId);
    }


    @Test
    void shouldCreateProduct() {
        ProductRequest request = mock(ProductRequest.class);

        when(request.getName()).thenReturn("T-Shirt");
        when(request.getCategoryId()).thenReturn(null);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct(request);

        assertEquals("T-Shirt", result.getName());

        verify(productRepository).save(any(Product.class));
        verifyNoInteractions(categoryRepository);
    }

}
