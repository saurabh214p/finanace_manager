package finance.management.service;


import finance.management.dto.request.SavingsGoalRequest;
import finance.management.dto.request.UpdateSavingsGoalRequest;
import finance.management.dto.response.SavingsGoalResponse;

import java.util.List;

/**
 * Service interface for savings goal operations.
 */
public interface SavingsGoalService {

    SavingsGoalResponse createGoal(String username, SavingsGoalRequest request);

    List<SavingsGoalResponse> getAllGoals(String username);

    SavingsGoalResponse getGoal(String username, Long id);

    SavingsGoalResponse updateGoal(String username, Long id, UpdateSavingsGoalRequest request);

    void deleteGoal(String username, Long id);
}
