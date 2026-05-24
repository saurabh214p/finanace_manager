package finance.management.controller;

import finance.management.dto.request.SavingsGoalRequest;
import finance.management.dto.request.UpdateSavingsGoalRequest;
import finance.management.dto.response.SavingsGoalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for savings goal management endpoints.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    /**
     * Creates a new savings goal.
     */
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.createGoal(
                userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns all savings goals for the current user.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllGoals(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoals(userDetails.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("goals", goals);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single savings goal by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> getGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        SavingsGoalResponse response = savingsGoalService.getGoal(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a savings goal's target amount and/or date.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.updateGoal(
                userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        savingsGoalService.deleteGoal(userDetails.getUsername(), id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Goal deleted successfully");
        return ResponseEntity.ok(response);
    }
}
