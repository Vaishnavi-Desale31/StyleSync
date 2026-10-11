package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.ProductRequest;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.service.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    // Create a product
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody ProductRequest request){
        return productService.createProduct(request);
    }

    // Get all products
    @GetMapping
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }

    // Get a product by ID
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    // Update a product
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id,
                                 @Valid @RequestBody ProductRequest request){
        return productService.updateProduct(id, request);
    }

    // Delete a product
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id){
      productService.deleteProduct(id);
    }

    // Search products by name
@GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name){
        return productService.searchProductsByName(name);
    }

    //Get products by category ID
    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategoryId(
            @PathVariable Long categoryId){
        return productService.getProductsByCategoryId(categoryId);
    }
}
