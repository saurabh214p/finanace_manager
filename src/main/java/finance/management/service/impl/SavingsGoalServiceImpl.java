package finance.management.service.impl;

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
import finance.management.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of savings goal operations.
 */
@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public SavingsGoalResponse createGoal(String username, SavingsGoalRequest request) {
        User user = getUser(username);

        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        SavingsGoal saved = goalRepository.save(goal);
        return toResponse(saved, user);
    }

    @Override
    public List<SavingsGoalResponse> getAllGoals(String username) {
        User user = getUser(username);
        return goalRepository.findByUser(user).stream()
                .map(g -> toResponse(g, user))
                .collect(Collectors.toList());
    }

    @Override
    public SavingsGoalResponse getGoal(String username, Long id) {
        User user = getUser(username);
        SavingsGoal goal = resolveGoal(id, user);
        return toResponse(goal, user);
    }

    @Override
    @Transactional
    public SavingsGoalResponse updateGoal(String username, Long id, UpdateSavingsGoalRequest request) {
        User user = getUser(username);
        SavingsGoal goal = resolveGoal(id, user);

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal saved = goalRepository.save(goal);
        return toResponse(saved, user);
    }

    @Override
    @Transactional
    public void deleteGoal(String username, Long id) {
        User user = getUser(username);
        SavingsGoal goal = resolveGoal(id, user);
        goalRepository.delete(goal);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Finds a goal by id for the given user.
     * Returns 404 if the goal doesn't exist at all, 403 if it belongs to another user.
     */
    private SavingsGoal resolveGoal(Long id, User user) {
        return goalRepository.findByIdAndUser(id, user).orElseThrow(() -> {
            if (goalRepository.existsById(id)) {
                throw new ForbiddenException("Access denied to goal: " + id);
            }
            throw new ResourceNotFoundException("Goal not found: " + id);
        });
    }

    /**
     * Converts a SavingsGoal entity to a SavingsGoalResponse DTO,
     * computing progress from transactions since the goal's start date.
     */
    public SavingsGoalResponse toResponse(SavingsGoal goal, User user) {
        LocalDate endDate = LocalDate.now();
        BigDecimal income = transactionRepository.sumIncomeByUserAndDateRange(
                user, goal.getStartDate(), endDate);
        BigDecimal expenses = transactionRepository.sumExpensesByUserAndDateRange(
                user, goal.getStartDate(), endDate);

        BigDecimal progress = income.subtract(expenses);
        if (progress.compareTo(BigDecimal.ZERO) < 0) {
            progress = BigDecimal.ZERO;
        }

        BigDecimal target = goal.getTargetAmount();
        double percentage = target.compareTo(BigDecimal.ZERO) > 0
                ? progress.multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP)
                .doubleValue()
                : 0.0;

        BigDecimal remaining = target.subtract(progress);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        return SavingsGoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(progress)
                .progressPercentage(percentage)
                .remainingAmount(remaining)
                .build();
    }
}

