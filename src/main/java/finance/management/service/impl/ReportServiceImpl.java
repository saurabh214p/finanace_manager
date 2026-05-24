package finance.management.service.impl;

import finance.management.dto.response.MonthlyReportResponse;
import finance.management.dto.response.YearlyReportResponse;
import finance.management.entity.User;
import finance.management.repository.TransactionRepository;
import finance.management.repository.UserRepository;
import finance.management.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of financial report generation.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public MonthlyReportResponse getMonthlyReport(String username, int year, int month) {
        User user = getUser(username);

        Map<String, BigDecimal> incomeMap = toMap(
                transactionRepository.sumIncomeGroupedByCategoryForMonth(user, year, month));
        Map<String, BigDecimal> expenseMap = toMap(
                transactionRepository.sumExpensesGroupedByCategoryForMonth(user, year, month));

        BigDecimal totalIncome = incomeMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(incomeMap)
                .totalExpenses(expenseMap)
                .netSavings(netSavings)
                .build();
    }

    @Override
    public YearlyReportResponse getYearlyReport(String username, int year) {
        User user = getUser(username);

        Map<String, BigDecimal> incomeMap = toMap(
                transactionRepository.sumIncomeGroupedByCategoryForYear(user, year));
        Map<String, BigDecimal> expenseMap = toMap(
                transactionRepository.sumExpensesGroupedByCategoryForYear(user, year));

        BigDecimal totalIncome = incomeMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeMap)
                .totalExpenses(expenseMap)
                .netSavings(netSavings)
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private Map<String, BigDecimal> toMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String categoryName = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            map.put(categoryName, total);
        }
        return map;
    }
}

