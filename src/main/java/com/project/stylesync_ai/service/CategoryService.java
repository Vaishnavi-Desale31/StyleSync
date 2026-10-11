package com.project.stylesync_ai.service;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.project.stylesync_ai.exception.ResourceNotFoundException;
import com.project.stylesync_ai.model.Category;
import com.project.stylesync_ai.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category){
        if(categoryRepository.findByNameIgnoreCase(category.getName())
                .isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists"
            );
        }
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id:" + id
                        ));
    }

    public Category updateCategory(Long id, Category updateCategory){
        Category existingCategory = getCategoryById(id);

        if(categoryRepository.findByNameIgnoreCase(updateCategory.getName())
                .filter(category -> !category.getId().equals(id))
                .isPresent()){
            throw  new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists"
            );
        }

        existingCategory.setName(updateCategory.getName());

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id){
        Category category = getCategoryById(id);

        categoryRepository.delete(category);
    }


}
