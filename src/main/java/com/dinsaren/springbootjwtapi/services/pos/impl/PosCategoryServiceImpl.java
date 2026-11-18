package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.Category;
import com.dinsaren.springbootjwtapi.payload.pos.CategoryRequest;
import com.dinsaren.springbootjwtapi.repository.pos.CategoryRepository;
import com.dinsaren.springbootjwtapi.repository.pos.PosProductRepository;
import com.dinsaren.springbootjwtapi.services.pos.PosCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PosCategoryServiceImpl implements PosCategoryService {

    private final CategoryRepository categoryRepository;
    private final PosProductRepository posProductRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    @Override
    @Transactional
    public Category createCategory(CategoryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        String name = request.getName().trim();
        if (categoryRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setIcon(request.getIcon());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Integer id, CategoryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        Category category = getCategoryById(id);
        String name = request.getName().trim();

        if (!name.equalsIgnoreCase(category.getName()) && categoryRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name already exists");
        }

        category.setName(name);
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = getCategoryById(id);

        long productCount = posProductRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete category that contains products. Please reassign or delete the products first.");
        }

        categoryRepository.delete(category);
    }
}
