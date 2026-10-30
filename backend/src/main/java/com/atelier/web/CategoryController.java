package com.atelier.web;

import com.atelier.api.CategoriesApi;
import com.atelier.api.model.CategoryDto;
import com.atelier.api.model.CategoryWriteDto;
import com.atelier.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController implements CategoriesApi {

    private final CategoryService categories;

    public CategoryController(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public ResponseEntity<List<CategoryDto>> listCategories() {
        return ResponseEntity.ok(categories.list());
    }

    @Override
    public ResponseEntity<CategoryDto> getCategory(Long categoryId) {
        return ResponseEntity.ok(categories.get(categoryId));
    }

    @Override
    public ResponseEntity<CategoryDto> createCategory(CategoryWriteDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categories.create(request));
    }

    @Override
    public ResponseEntity<CategoryDto> updateCategory(Long categoryId, CategoryWriteDto request) {
        return ResponseEntity.ok(categories.update(categoryId, request));
    }

    @Override
    public ResponseEntity<Void> deleteCategory(Long categoryId) {
        categories.delete(categoryId);
        return ResponseEntity.noContent().build();
    }
}
