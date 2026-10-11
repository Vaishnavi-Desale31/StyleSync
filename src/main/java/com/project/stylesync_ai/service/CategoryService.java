package com.project.stylesync_ai.service;


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

        existingCategory.setName(updateCategory.getName());

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id){
        Category category = getCategoryById(id);

        categoryRepository.delete(category);
    }


}
