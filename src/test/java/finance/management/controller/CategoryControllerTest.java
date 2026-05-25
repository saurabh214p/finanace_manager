package finance.management.controller;

import finance.management.dto.request.CategoryRequest;
import finance.management.dto.response.CategoryResponse;
import finance.management.entity.CategoryType;
import finance.management.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock private CategoryService categoryService;
    @InjectMocks private CategoryController categoryController;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("user@example.com", "password", Collections.emptyList());
    }

    @Test
    void getAllCategories_returns200WithList() {
        CategoryResponse cat = CategoryResponse.builder()
                .name("Salary").type(CategoryType.INCOME).isCustom(false).build();
        when(categoryService.getAllCategories(anyString())).thenReturn(List.of(cat));

        ResponseEntity<Map<String, Object>> response = categoryController.getAllCategories(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("categories");
    }

    @Test
    void createCategory_returns201() {
        CategoryRequest req = new CategoryRequest();
        req.setName("SideIncome");
        req.setType(CategoryType.INCOME);

        CategoryResponse created = CategoryResponse.builder()
                .name("SideIncome").type(CategoryType.INCOME).isCustom(true).build();
        when(categoryService.createCategory(anyString(), any())).thenReturn(created);

        ResponseEntity<CategoryResponse> response = categoryController.createCategory(userDetails, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("SideIncome");
    }

    @Test
    void deleteCategory_returns200WithMessage() {
        doNothing().when(categoryService).deleteCategory(anyString(), eq("Freelance"));

        ResponseEntity<Map<String, String>> response = categoryController.deleteCategory(userDetails, "Freelance");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Category deleted successfully");
    }
}