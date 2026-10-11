package com.project.stylesync_ai.service;

import com.project.stylesync_ai.dto.ProductRequest;
import com.project.stylesync_ai.exception.ResourceNotFoundException;

import com.project.stylesync_ai.model.Category;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.repository.CategoryRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository){
        this.productRepository=productRepository;
        this.categoryRepository=categoryRepository;
    }

    //Create a new product assign a category
    public Product createProduct(ProductRequest request){
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());
        product.setImageUrl(request.getImageUrl());
        product.setSize(request.getSize());
        product.setColor(request.getColor());

        if(request.getCategoryId() != null){
            Category category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Category not found with id: "
                                    + request.getCategoryId()
                            ));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    //Get all products
    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    //Get a product by ID
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id:" + id));

    }

    //Update a product and its category
    public Product updateProduct(Long id, ProductRequest request){
        Product existingProduct = getProductById(id);

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setImageUrl(request.getImageUrl());
        existingProduct.setSize(request.getSize());
        existingProduct.setColor(request.getColor());

        if(request.getCategoryId() != null){
            Category category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Category not found with id: "
                                    + request.getCategoryId()
                            ));

            existingProduct.setCategory(category);
        }

        return productRepository.save(existingProduct);
    }

    //Delete a product
    public void deleteProduct(Long id){
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    //Search products by name
    public List<Product> searchProductsByName(String name){
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    //Get products by category ID
    public List<Product> getProductsByCategoryId(Long categoryId){
        return productRepository.findByCategoryId(categoryId);
    }
}
