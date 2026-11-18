package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.payload.pos.ProductRequest;

import java.util.List;
import java.util.Map;

public interface PosProductService {
    List<PosProduct> searchProducts(String query, String categoryIdStr);
    PosProduct getProductById(Integer id);
    PosProduct createProduct(ProductRequest request, Integer currentUserId);
    PosProduct updateProduct(Integer id, ProductRequest request, Integer currentUserId);
    void deleteProduct(Integer id);
    Map<String, Object> bulkImport(List<Map<String, Object>> products, Integer currentUserId);
}
