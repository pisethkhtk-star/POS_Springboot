package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Category;
import com.dinsaren.springbootjwtapi.payload.pos.CategoryRequest;

import java.util.List;

public interface PosCategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Integer id);
    Category createCategory(CategoryRequest request);
    Category updateCategory(Integer id, CategoryRequest request);
    void deleteCategory(Integer id);
}
