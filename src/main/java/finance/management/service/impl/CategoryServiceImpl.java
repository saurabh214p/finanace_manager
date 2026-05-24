package finance.management.service.impl;

import finance.management.dto.response.CategoryResponse;
import finance.management.entity.Category;
import finance.management.entity.User;
import finance.management.exception.BadRequestException;
import finance.management.exception.ForbiddenException;
import finance.management.exception.ResourceNotFoundException;
import finance.management.repository.CategoryRepository;
import finance.management.repository.TransactionRepository;
import finance.management.repository.UserRepository;
import finance.management.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of category management operations.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<CategoryResponse> getAllCategories(String username) {
        User user = getUser(username);

        List<Category> defaults = categoryRepository.findByCustomFalse();
        List<Category> custom = categoryRepository.findByUserAndCustomTrue(user);

        List<CategoryResponse> result = new ArrayList<>();
        defaults.stream().map(this::toResponse).forEach(result::add);
        custom.stream().map(this::toResponse).forEach(result::add);
        return result;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(String username, CategoryRequest request) {
        User user = getUser(username);

        // Check uniqueness across default and user categories
        if (categoryRepository.existsByNameIgnoreCaseAndUser(request.getName(), user)) {
            throw new ConflictException("Custom category already exists: " + request.getName());
        }
        if (categoryRepository.findByNameIgnoreCaseAndCustomFalse(request.getName()).isPresent()) {
            throw new ConflictException("A default category with that name already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .custom(true)
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(String username, String name) {
        User user = getUser(username);

        // Try to find as a default category first (forbidden to delete)
        categoryRepository.findByNameIgnoreCaseAndCustomFalse(name).ifPresent(c -> {
            throw new ForbiddenException("Default categories cannot be deleted");
        });

        // Find custom category belonging to user
        Category category = categoryRepository.findByNameIgnoreCaseAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));

        if (transactionRepository.existsByCategory(category)) {
            throw new BadRequestException(
                    "Cannot delete category that is referenced by existing transactions");
        }

        categoryRepository.delete(category);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Maps a Category entity to a CategoryResponse DTO.
     */
    public CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .name(c.getName())
                .type(c.getType())
                .isCustom(c.isCustom())
                .build();
    }
}
