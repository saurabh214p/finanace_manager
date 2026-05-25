package finance.management.service;
import finance.management.dto.request.CategoryRequest;
import finance.management.dto.response.CategoryResponse;
import finance.management.entity.Category;
import finance.management.entity.CategoryType;
import finance.management.entity.User;
import finance.management.exception.BadRequestException;
import finance.management.exception.ConflictException;
import finance.management.exception.ForbiddenException;
import finance.management.exception.ResourceNotFoundException;
import finance.management.repository.CategoryRepository;
import finance.management.repository.TransactionRepository;
import finance.management.repository.UserRepository;
import finance.management.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private CategoryServiceImpl categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@example.com").build();
        defaultCategory = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).custom(false).build();
        customCategory = Category.builder().id(2L).name("Freelance").type(CategoryType.INCOME).custom(true).user(user).build();
    }

    @Test
    void getAllCategories_returnsBothDefaultAndCustom() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findByCustomFalse()).thenReturn(List.of(defaultCategory));
        when(categoryRepository.findByUserAndCustomTrue(user)).thenReturn(List.of(customCategory));

        List<CategoryResponse> result = categoryService.getAllCategories("user@example.com");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).isCustom()).isFalse();
        assertThat(result.get(1).isCustom()).isTrue();
    }

    @Test
    void getAllCategories_onlyDefaults_whenNoCustom() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findByCustomFalse()).thenReturn(List.of(defaultCategory));
        when(categoryRepository.findByUserAndCustomTrue(user)).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAllCategories("user@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Salary");
    }

    @Test
    void createCategory_success() {
        CategoryRequest req = new CategoryRequest();
        req.setName("SideIncome");
        req.setType(CategoryType.INCOME);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameIgnoreCaseAndUser("SideIncome", user)).thenReturn(false);
        when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("SideIncome")).thenReturn(Optional.empty());

        Category saved = Category.builder().name("SideIncome").type(CategoryType.INCOME).custom(true).user(user).build();
        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryResponse result = categoryService.createCategory("user@example.com", req);

        assertThat(result.getName()).isEqualTo("SideIncome");
        assertThat(result.isCustom()).isTrue();
        assertThat(result.getType()).isEqualTo(CategoryType.INCOME);
    }

    @Test
    void createCategory_duplicateCustom_throwsConflict() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Freelance");
        req.setType(CategoryType.INCOME);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameIgnoreCaseAndUser("Freelance", user)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory("user@example.com", req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createCategory_sameNameAsDefault_throwsConflict() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Salary");
        req.setType(CategoryType.INCOME);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.existsByNameIgnoreCaseAndUser("Salary", user)).thenReturn(false);
        when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Salary")).thenReturn(Optional.of(defaultCategory));

        assertThatThrownBy(() -> categoryService.createCategory("user@example.com", req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteCategory_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);

        categoryService.deleteCategory("user@example.com", "Freelance");

        verify(categoryRepository).delete(customCategory);
    }

    @Test
    void deleteCategory_defaultCategory_throwsForbidden() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Salary")).thenReturn(Optional.of(defaultCategory));

        assertThatThrownBy(() -> categoryService.deleteCategory("user@example.com", "Salary"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteCategory_hasTransactions_throwsBadRequest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("user@example.com", "Freelance"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteCategory_notFound_throwsNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Unknown")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCaseAndUser("Unknown", user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory("user@example.com", "Unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
