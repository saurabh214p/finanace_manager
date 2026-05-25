package finance.management.service;


import finance.management.dto.request.SavingsGoalRequest;
import finance.management.dto.request.UpdateSavingsGoalRequest;
import finance.management.dto.response.SavingsGoalResponse;
import finance.management.entity.SavingsGoal;
import finance.management.entity.User;
import finance.management.exception.BadRequestException;
import finance.management.exception.ForbiddenException;
import finance.management.exception.ResourceNotFoundException;
import finance.management.repository.SavingsGoalRepository;
import finance.management.repository.TransactionRepository;
import finance.management.repository.UserRepository;
import finance.management.service.impl.SavingsGoalServiceImpl;
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
class SavingsGoalServiceTest {

    @Mock private SavingsGoalRepository goalRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private SavingsGoalServiceImpl savingsGoalService;

    private User user;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@example.com").build();
        goal = SavingsGoal.builder()
                .id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now())
                .user(user).build();
    }

    @Test
    void createGoal_success() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(new BigDecimal("5000"));
        req.setTargetDate(LocalDate.now().plusMonths(6));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.save(any())).thenReturn(goal);
        when(transactionRepository.sumIncomeByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);

        SavingsGoalResponse result = savingsGoalService.createGoal("user@example.com", req);

        assertThat(result.getGoalName()).isEqualTo("Emergency Fund");
        assertThat(result.getProgressPercentage()).isEqualTo(0.0);
        assertThat(result.getCurrentProgress()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createGoal_withStartDate_usesProvided() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        req.setGoalName("Car Fund");
        req.setTargetAmount(new BigDecimal("10000"));
        req.setTargetDate(LocalDate.now().plusYears(1));
        req.setStartDate(LocalDate.of(2025, 1, 1));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.save(any())).thenReturn(goal);
        when(transactionRepository.sumIncomeByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);

        savingsGoalService.createGoal("user@example.com", req);

        verify(goalRepository).save(any());
    }

    @Test
    void createGoal_pastTargetDate_throwsBadRequest() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        req.setGoalName("Test");
        req.setTargetAmount(new BigDecimal("1000"));
        req.setTargetDate(LocalDate.now().minusDays(1));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> savingsGoalService.createGoal("user@example.com", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    void getAllGoals_returnsAllWithProgress() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(transactionRepository.sumIncomeByUserAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("1000"));
        when(transactionRepository.sumExpensesByUserAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("200"));

        List<SavingsGoalResponse> result = savingsGoalService.getAllGoals("user@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrentProgress()).isEqualByComparingTo(new BigDecimal("800"));
        assertThat(result.get(0).getRemainingAmount()).isEqualByComparingTo(new BigDecimal("4200"));
    }

    @Test
    void getAllGoals_progressExceedsTarget_remainingIsZero() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(transactionRepository.sumIncomeByUserAndDateRange(any(), any(), any())).thenReturn(new BigDecimal("6000"));
        when(transactionRepository.sumExpensesByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);

        List<SavingsGoalResponse> result = savingsGoalService.getAllGoals("user@example.com");

        assertThat(result.get(0).getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getGoal_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.sumIncomeByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);

        SavingsGoalResponse result = savingsGoalService.getGoal("user@example.com", 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getGoal_notFound_throwsNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());
        when(goalRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> savingsGoalService.getGoal("user@example.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getGoal_belongsToOtherUser_throwsForbidden() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());
        when(goalRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> savingsGoalService.getGoal("user@example.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateGoal_targetAmount_success() {
        UpdateSavingsGoalRequest req = new UpdateSavingsGoalRequest();
        req.setTargetAmount(new BigDecimal("6000"));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenReturn(goal);
        when(transactionRepository.sumIncomeByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpensesByUserAndDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);

        savingsGoalService.updateGoal("user@example.com", 1L, req);

        assertThat(goal.getTargetAmount()).isEqualByComparingTo(new BigDecimal("6000"));
    }

    @Test
    void updateGoal_pastTargetDate_throwsBadRequest() {
        UpdateSavingsGoalRequest req = new UpdateSavingsGoalRequest();
        req.setTargetDate(LocalDate.now().minusDays(1));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> savingsGoalService.updateGoal("user@example.com", 1L, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteGoal_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));

        savingsGoalService.deleteGoal("user@example.com", 1L);

        verify(goalRepository).delete(goal);
    }

    @Test
    void deleteGoal_notFound_throwsNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());
        when(goalRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> savingsGoalService.deleteGoal("user@example.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteGoal_belongsToOtherUser_throwsForbidden() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());
        when(goalRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> savingsGoalService.deleteGoal("user@example.com", 1L))
                .isInstanceOf(ForbiddenException.class);
    }
}