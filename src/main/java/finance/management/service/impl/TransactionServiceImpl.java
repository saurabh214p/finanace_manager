package finance.management.service.impl;

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
import finance.management.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of transaction CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(String username, TransactionRequest request) {
        User user = getUser(username);

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        Category category = categoryRepository.findAccessibleByName(request.getCategory(), user)
                .orElseThrow(() -> new BadRequestException(
                        "Category not found or not accessible: " + request.getCategory()));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .description(request.getDescription())
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    public List<TransactionResponse> getTransactions(String username, LocalDate startDate,
                                                     LocalDate endDate, Long categoryId, CategoryType type) {
        User user = getUser(username);

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findAccessibleById(categoryId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        }

        return transactionRepository.findByUserWithFilters(user, startDate, endDate, category, type)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest request) {
        User user = getUser(username);

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findAccessibleByName(request.getCategory(), user)
                    .orElseThrow(() -> new BadRequestException(
                            "Category not found or not accessible: " + request.getCategory()));
            transaction.setCategory(category);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTransaction(String username, Long id) {
        User user = getUser(username);

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        transactionRepository.delete(transaction);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Maps a Transaction entity to a TransactionResponse DTO.
     */
    public TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .date(t.getDate())
                .category(t.getCategory().getName())
                .description(t.getDescription())
                .type(t.getCategory().getType())
                .build();
    }
}

