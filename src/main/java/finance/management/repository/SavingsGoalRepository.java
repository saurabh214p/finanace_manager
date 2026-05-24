package finance.management.repository;

import finance.management.entity.SavingsGoal;
import finance.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SavingsGoal entity persistence operations.
 */
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUser(User user);

    Optional<SavingsGoal> findByIdAndUser(Long id, User user);

    /** Used to distinguish 404 (not found) from 403 (found but belongs to another user). */
    boolean existsById(Long id);
}