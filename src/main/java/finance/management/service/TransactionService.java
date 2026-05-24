package finance.management.service;


import finance.management.dto.request.TransactionRequest;
import finance.management.dto.request.UpdateTransactionRequest;
import finance.management.dto.response.TransactionResponse;
import finance.management.entity.CategoryType;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for transaction CRUD operations.
 */
public interface TransactionService {

    TransactionResponse createTransaction(String username, TransactionRequest request);

    List<TransactionResponse> getTransactions(String username, LocalDate startDate,
                                              LocalDate endDate, Long categoryId, CategoryType type);

    TransactionResponse updateTransaction(String username, Long id, UpdateTransactionRequest request);

    void deleteTransaction(String username, Long id);
}

