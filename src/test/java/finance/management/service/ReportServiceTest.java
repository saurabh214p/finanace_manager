package finance.management.service;


import finance.management.dto.response.MonthlyReportResponse;
import finance.management.dto.response.YearlyReportResponse;
import finance.management.entity.User;
import finance.management.repository.TransactionRepository;
import finance.management.repository.UserRepository;
import finance.management.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ReportServiceImpl reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@example.com").build();
    }

    private List<Object[]> rows(Object[]... items) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] item : items) {
            list.add(item);
        }
        return list;
    }

    @Test
    void getMonthlyReport_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.sumIncomeGroupedByCategoryForMonth(user, 2024, 1))
                .thenReturn(rows(new Object[]{"Salary", new BigDecimal("3000")}));
        when(transactionRepository.sumExpensesGroupedByCategoryForMonth(user, 2024, 1))
                .thenReturn(rows(new Object[]{"Food", new BigDecimal("500")}));

        MonthlyReportResponse result = reportService.getMonthlyReport("user@example.com", 2024, 1);

        assertThat(result.getMonth()).isEqualTo(1);
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getTotalIncome()).containsEntry("Salary", new BigDecimal("3000"));
        assertThat(result.getTotalExpenses()).containsEntry("Food", new BigDecimal("500"));
        assertThat(result.getNetSavings()).isEqualByComparingTo(new BigDecimal("2500"));
    }

    @Test
    void getMonthlyReport_noTransactions_zeroNetSavings() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.sumIncomeGroupedByCategoryForMonth(user, 2024, 3))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.sumExpensesGroupedByCategoryForMonth(user, 2024, 3))
                .thenReturn(new ArrayList<>());

        MonthlyReportResponse result = reportService.getMonthlyReport("user@example.com", 2024, 3);

        assertThat(result.getNetSavings()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalIncome()).isEmpty();
        assertThat(result.getTotalExpenses()).isEmpty();
    }

    @Test
    void getMonthlyReport_expensesExceedIncome_negativeNetSavings() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.sumIncomeGroupedByCategoryForMonth(user, 2024, 6))
                .thenReturn(rows(new Object[]{"Salary", new BigDecimal("1000")}));
        when(transactionRepository.sumExpensesGroupedByCategoryForMonth(user, 2024, 6))
                .thenReturn(rows(new Object[]{"Rent", new BigDecimal("2000")}));

        MonthlyReportResponse result = reportService.getMonthlyReport("user@example.com", 2024, 6);

        assertThat(result.getNetSavings()).isEqualByComparingTo(new BigDecimal("-1000"));
    }

    @Test
    void getMonthlyReport_multipleCategories() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.sumIncomeGroupedByCategoryForMonth(user, 2024, 2))
                .thenReturn(rows(
                        new Object[]{"Salary", new BigDecimal("3000")},
                        new Object[]{"Freelance", new BigDecimal("500")}));
        when(transactionRepository.sumExpensesGroupedByCategoryForMonth(user, 2024, 2))
                .thenReturn(rows(
                        new Object[]{"Food", new BigDecimal("400")},
                        new Object[]{"Rent", new BigDecimal("1200")}));

        MonthlyReportResponse result = reportService.getMonthlyReport("user@example.com", 2024, 2);

        assertThat(result.getTotalIncome()).hasSize(2);
        assertThat(result.getTotalExpenses()).hasSize(2);
        assertThat(result.getNetSavings()).isEqualByComparingTo(new BigDecimal("1900"));
    }

    @Test
    void getYearlyReport_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.sumIncomeGroupedByCategoryForYear(user, 2024))
                .thenReturn(rows(new Object[]{"Salary", new BigDecimal("36000")}));
        when(transactionRepository.sumExpensesGroupedByCategoryForYear(user, 2024))
                .thenReturn(rows(new Object[]{"Rent", new BigDecimal("14400")}));

        YearlyReportResponse result = reportService.getYearlyReport("user@example.com", 2024);

        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getTotalIncome()).containsEntry("Salary", new BigDecimal("36000"));
        assertThat(result.getNetSavings()).isEqualByComparingTo(new BigDecimal("21600"));
    }

    @Test
    void getYearlyReport_noTransactions_zeroNetSavings() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.sumIncomeGroupedByCategoryForYear(user, 2023))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.sumExpensesGroupedByCategoryForYear(user, 2023))
                .thenReturn(new ArrayList<>());

        YearlyReportResponse result = reportService.getYearlyReport("user@example.com", 2023);

        assertThat(result.getNetSavings()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getYearlyReport_expensesExceedIncome_negativeNetSavings() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.sumIncomeGroupedByCategoryForYear(user, 2024))
                .thenReturn(rows(new Object[]{"Salary", new BigDecimal("1000")}));
        when(transactionRepository.sumExpensesGroupedByCategoryForYear(user, 2024))
                .thenReturn(rows(new Object[]{"Rent", new BigDecimal("2000")}));

        YearlyReportResponse result = reportService.getYearlyReport("user@example.com", 2024);

        assertThat(result.getNetSavings()).isEqualByComparingTo(new BigDecimal("-1000"));
    }
}