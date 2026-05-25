package finance.management.controller;

import finance.management.dto.request.SavingsGoalRequest;
import finance.management.dto.request.UpdateSavingsGoalRequest;
import finance.management.dto.response.SavingsGoalResponse;
import finance.management.service.SavingsGoalService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalControllerTest {

    @Mock private SavingsGoalService savingsGoalService;
    @InjectMocks private SavingsGoalController savingsGoalController;

    private UserDetails userDetails;
    private SavingsGoalResponse sampleGoal;

    @BeforeEach
    void setUp() {
        userDetails = new User("user@example.com", "password", Collections.emptyList());
        sampleGoal = SavingsGoalResponse.builder()
                .id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now())
                .currentProgress(BigDecimal.ZERO)
                .progressPercentage(0.0)
                .remainingAmount(new BigDecimal("5000"))
                .build();
    }

    @Test
    void createGoal_returns201() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        when(savingsGoalService.createGoal(anyString(), any())).thenReturn(sampleGoal);

        ResponseEntity<SavingsGoalResponse> response = savingsGoalController.createGoal(userDetails, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getGoalName()).isEqualTo("Emergency Fund");
    }

    @Test
    void getAllGoals_returns200WithList() {
        when(savingsGoalService.getAllGoals(anyString())).thenReturn(List.of(sampleGoal));

        ResponseEntity<Map<String, Object>> response = savingsGoalController.getAllGoals(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("goals");
    }

    @Test
    void getGoal_returns200() {
        when(savingsGoalService.getGoal(anyString(), eq(1L))).thenReturn(sampleGoal);

        ResponseEntity<SavingsGoalResponse> response = savingsGoalController.getGoal(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void updateGoal_returns200() {
        UpdateSavingsGoalRequest req = new UpdateSavingsGoalRequest();
        when(savingsGoalService.updateGoal(anyString(), eq(1L), any())).thenReturn(sampleGoal);

        ResponseEntity<SavingsGoalResponse> response = savingsGoalController.updateGoal(userDetails, 1L, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteGoal_returns200WithMessage() {
        doNothing().when(savingsGoalService).deleteGoal(anyString(), eq(1L));

        ResponseEntity<Map<String, String>> response = savingsGoalController.deleteGoal(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Goal deleted successfully");
    }
}