package com.atelier.service;

import com.atelier.api.model.CategoryDto;
import com.atelier.api.model.CategoryWriteDto;
import com.atelier.domain.Category;
import com.atelier.error.ApiException;
import com.atelier.repo.CategoryRepository;
import com.atelier.repo.ProductRepository;
import com.atelier.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ApiMapper mapper;

    public CategoryService(CategoryRepository categories, ProductRepository products, ApiMapper mapper) {
        this.categories = categories;
        this.products = products;
        this.mapper = mapper;
    }

    public List<CategoryDto> list() {
        return categories.findAll().stream().map(mapper::toCategory).toList();
    }

    public CategoryDto get(Long categoryId) {
        return mapper.toCategory(require(categoryId));
    }

    @Transactional
    public CategoryDto create(CategoryWriteDto request) {
        AuthContext.requireAdmin();
        String name = request.getName().trim();
        if (categories.existsByNameIgnoreCase(name)) {
            throw ApiException.unprocessable("ERR_DUPLICATE_CATEGORY", "Danh mục đã tồn tại",
                    List.of("name: Danh mục đã tồn tại"));
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(Texts.blankToNull(request.getDescription()));
        return mapper.toCategory(categories.save(category));
    }

    @Transactional
    public CategoryDto update(Long categoryId, CategoryWriteDto request) {
        AuthContext.requireAdmin();
        Category category = require(categoryId);
        String name = request.getName().trim();
        if (categories.existsOtherName(name, categoryId)) {
            throw ApiException.unprocessable("ERR_DUPLICATE_CATEGORY", "Danh mục đã tồn tại",
                    List.of("name: Danh mục đã tồn tại"));
        }
        category.setName(name);
        category.setDescription(Texts.blankToNull(request.getDescription()));
        return mapper.toCategory(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        AuthContext.requireAdmin();
        Category category = require(categoryId);
        if (products.existsByCategoryId(categoryId)) {
            throw ApiException.badRequest("ERR_CATEGORY_IN_USE", "Danh mục còn sản phẩm, không thể xóa");
        }
        categories.delete(category);
    }

    private Category require(Long categoryId) {
        return categories.findById(categoryId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy danh mục"));
    }
}
