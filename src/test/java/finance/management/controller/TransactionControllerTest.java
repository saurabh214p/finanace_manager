package finance.management.controller;

import finance.management.dto.request.TransactionRequest;
import finance.management.dto.request.UpdateTransactionRequest;
import finance.management.dto.response.TransactionResponse;
import finance.management.entity.CategoryType;
import finance.management.service.TransactionService;
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
class TransactionControllerTest {

    @Mock private TransactionService transactionService;
    @InjectMocks private TransactionController transactionController;

    private UserDetails userDetails;
    private TransactionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        userDetails = new User("user@example.com", "password", Collections.emptyList());
        sampleResponse = TransactionResponse.builder()
                .id(1L).amount(new BigDecimal("1000"))
                .date(LocalDate.now()).category("Salary")
                .type(CategoryType.INCOME).build();
    }

    @Test
    void createTransaction_returns201() {
        TransactionRequest req = new TransactionRequest();
        when(transactionService.createTransaction(anyString(), any())).thenReturn(sampleResponse);

        ResponseEntity<TransactionResponse> response = transactionController.createTransaction(userDetails, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    void getTransactions_returns200WithList() {
        when(transactionService.getTransactions(anyString(), any(), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<Map<String, Object>> response = transactionController.getTransactions(
                userDetails, null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("transactions");
        @SuppressWarnings("unchecked")
        List<TransactionResponse> transactions = (List<TransactionResponse>) response.getBody().get("transactions");
        assertThat(transactions).hasSize(1);
    }

    @Test
    void getTransactions_withTypeFilter() {
        when(transactionService.getTransactions(anyString(), any(), any(), any(), eq(CategoryType.INCOME)))
                .thenReturn(List.of(sampleResponse));

        ResponseEntity<Map<String, Object>> response = transactionController.getTransactions(
                userDetails, null, null, null, CategoryType.INCOME);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateTransaction_returns200() {
        UpdateTransactionRequest req = new UpdateTransactionRequest();
        when(transactionService.updateTransaction(anyString(), eq(1L), any())).thenReturn(sampleResponse);

        ResponseEntity<TransactionResponse> response = transactionController.updateTransaction(userDetails, 1L, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    void deleteTransaction_returns200WithMessage() {
        doNothing().when(transactionService).deleteTransaction(anyString(), eq(1L));

        ResponseEntity<Map<String, String>> response = transactionController.deleteTransaction(userDetails, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Transaction deleted successfully");
    }
}