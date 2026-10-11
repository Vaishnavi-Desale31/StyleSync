package com.project.stylesync_ai.service;

import com.project.stylesync_ai.exception.ResourceNotFoundException;
import com.project.stylesync_ai.model.Product;
import com.project.stylesync_ai.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository=productRepository;
    }

    //Create a new product
    public Product createProduct(Product product){
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

    //Update an existing product
    public Product updateProduct(Long id, Product updateProduct){
        Product existingProduct = getProductById(id);

        existingProduct.setName(updateProduct.getName());
        existingProduct.setDescription(updateProduct.getDescription());
        existingProduct.setPrice(updateProduct.getPrice());
        existingProduct.setBrand(updateProduct.getBrand());
        existingProduct.setImageUrl(updateProduct.getImageUrl());
        existingProduct.setSize(updateProduct.getSize());
        existingProduct.setColor(updateProduct.getColor());

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
}
