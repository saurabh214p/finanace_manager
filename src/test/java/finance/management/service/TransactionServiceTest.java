package finance.management.service;

import finance.management.dto.request.TransactionRequest;
import finance.management.dto.request.UpdateTransactionRequest;
import finance.management.dto.response.TransactionResponse;
import finance.management.entity.Category;
import finance.management.entity.CategoryType;
import finance.management.entity.Transaction;
import finance.management.entity.User;
import finance.management.exception.BadRequestException;
import finance.management.exception.ResourceNotFoundException;
import finance.management.repository.CategoryRepository;
import finance.management.repository.TransactionRepository;
import finance.management.repository.UserRepository;
import finance.management.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private TransactionServiceImpl transactionService;

    private User user;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@example.com").build();
        incomeCategory = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).custom(false).build();
        expenseCategory = Category.builder().id(2L).name("Food").type(CategoryType.EXPENSE).custom(false).build();
    }

    @Test
    void createTransaction_success() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("5000.00"));
        req.setDate(LocalDate.now());
        req.setCategory("Salary");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findAccessibleByName("Salary", user)).thenReturn(Optional.of(incomeCategory));

        Transaction saved = Transaction.builder()
                .id(1L).amount(req.getAmount()).date(req.getDate())
                .category(incomeCategory).user(user).build();
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse result = transactionService.createTransaction("user@example.com", req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(CategoryType.INCOME);
    }

    @Test
    void createTransaction_futureDate_throwsBadRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("100.00"));
        req.setDate(LocalDate.now().plusDays(1));
        req.setCategory("Salary");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> transactionService.createTransaction("user@example.com", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    void createTransaction_invalidCategory_throwsBadRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("100.00"));
        req.setDate(LocalDate.now());
        req.setCategory("Unknown");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findAccessibleByName("Unknown", user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction("user@example.com", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getTransactions_noFilters_returnsAll() {
        Transaction t = Transaction.builder().id(1L).amount(new BigDecimal("100"))
                .date(LocalDate.now()).category(expenseCategory).user(user).build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserWithFilters(eq(user), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactions(
                "user@example.com", null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CategoryType.EXPENSE);
    }

    @Test
    void getTransactions_withTypeFilter_returnsFiltered() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserWithFilters(
                eq(user), isNull(), isNull(), isNull(), eq(CategoryType.INCOME)))
                .thenReturn(List.of());

        List<TransactionResponse> result = transactionService.getTransactions(
                "user@example.com", null, null, null, CategoryType.INCOME);

        assertThat(result).isEmpty();
    }

    @Test
    void getTransactions_withDateRange() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);
        Transaction t = Transaction.builder().id(1L).amount(new BigDecimal("200"))
                .date(LocalDate.of(2024, 1, 15)).category(incomeCategory).user(user).build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserWithFilters(eq(user), eq(start), eq(end), isNull(), isNull()))
                .thenReturn(List.of(t));

        List<TransactionResponse> result = transactionService.getTransactions(
                "user@example.com", start, end, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("200"));
    }

    @Test
    void updateTransaction_amount_success() {
        Transaction existing = Transaction.builder().id(1L).amount(new BigDecimal("100"))
                .date(LocalDate.now()).category(incomeCategory).user(user).build();

        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setAmount(new BigDecimal("200"));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any())).thenReturn(existing);

        TransactionResponse result = transactionService.updateTransaction("user@example.com", 1L, req);

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("200"));
    }

    @Test
    void updateTransaction_category_success() {
        Transaction existing = Transaction.builder().id(1L).amount(new BigDecimal("100"))
                .date(LocalDate.now()).category(incomeCategory).user(user).build();

        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setCategory("Food");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(categoryRepository.findAccessibleByName("Food", user)).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any())).thenReturn(existing);

        transactionService.updateTransaction("user@example.com", 1L, req);

        assertThat(existing.getCategory()).isEqualTo(expenseCategory);
    }

    @Test
    void updateTransaction_description_success() {
        Transaction existing = Transaction.builder().id(1L).amount(new BigDecimal("100"))
                .date(LocalDate.now()).category(incomeCategory).user(user).build();

        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setDescription("Updated description");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any())).thenReturn(existing);

        transactionService.updateTransaction("user@example.com", 1L, req);

        assertThat(existing.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void updateTransaction_notFound_throwsNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(
                "user@example.com", 99L, new UpdateTransactionRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTransaction_success() {
        Transaction existing = Transaction.builder().id(1L).amount(new BigDecimal("100"))
                .date(LocalDate.now()).category(incomeCategory).user(user).build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction("user@example.com", 1L);

        verify(transactionRepository).delete(existing);
    }

    @Test
    void deleteTransaction_notFound_throwsNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction("user@example.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
