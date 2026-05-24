package finance.management.service;

import finance.management.dto.request.CategoryRequest;
import finance.management.dto.response.CategoryResponse;

import java.util.List;

/**
 * Service interface for category management operations.
 */
public interface CategoryService {

    List<CategoryResponse> getAllCategories(String username);

    CategoryResponse createCategory(String username, CategoryRequest request);

    void deleteCategory(String username, String name);
}

