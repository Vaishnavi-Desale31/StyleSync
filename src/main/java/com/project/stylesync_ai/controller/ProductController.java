package com.project.stylesync_ai.controller;

import com.project.stylesync_ai.dto.ProductRequest;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // Get products with pagination
    @GetMapping("/page")
    public Page<Product> getProductsWithPagination(
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size){

        if(page < 0){
            throw new IllegalArgumentException(
                    "Page number cannot ne negaive"
            );
        }

        if(size < 1 || size > 100){
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return productService.getProductsWithPagination(pageable);
    }

    // Get products sorted by a field
    @GetMapping("/sort")
    public List<Product> getProductsSorted(
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){

        //Validate the sorting field
        List<String> allowedFields = List.of(
                "name",
                "price",
                "id"
        );

        if(!allowedFields.contains(sortBy)){
            throw new IllegalArgumentException(
                    "Invalid sort field. Allowed fields : name, price, id"
            );
        }

        //Validate the sorting direction
        if(!direction.equalsIgnoreCase("asc")
        && !direction.equalsIgnoreCase("desc")){
            throw new IllegalArgumentException(
                    "Invalid sort direction. Use asc or desc"
            );
        }
        return productService.getProductsSorted(
                sortBy,
                direction.toLowerCase());
    }
}
