package finance.management.controller;

import finance.management.dto.response.MonthlyReportResponse;
import finance.management.dto.response.YearlyReportResponse;
import finance.management.exception.BadRequestException;
import finance.management.service.ReportService;
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
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock private ReportService reportService;
    @InjectMocks private ReportController reportController;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("user@example.com", "password", Collections.emptyList());
    }

    @Test
    void getMonthlyReport_returns200() {
        MonthlyReportResponse report = MonthlyReportResponse.builder()
                .month(1).year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("3000")))
                .totalExpenses(Map.of("Food", new BigDecimal("500")))
                .netSavings(new BigDecimal("2500")).build();
        when(reportService.getMonthlyReport(anyString(), eq(2024), eq(1))).thenReturn(report);

        ResponseEntity<MonthlyReportResponse> response = reportController.getMonthlyReport(userDetails, 2024, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMonth()).isEqualTo(1);
        assertThat(response.getBody().getNetSavings()).isEqualByComparingTo(new BigDecimal("2500"));
    }

    @Test
    void getMonthlyReport_invalidMonth_throwsBadRequest() {
        assertThatThrownBy(() -> reportController.getMonthlyReport(userDetails, 2024, 13))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> reportController.getMonthlyReport(userDetails, 2024, 0))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getYearlyReport_returns200() {
        YearlyReportResponse report = YearlyReportResponse.builder()
                .year(2024)
                .totalIncome(Map.of("Salary", new BigDecimal("36000")))
                .totalExpenses(Map.of("Rent", new BigDecimal("14400")))
                .netSavings(new BigDecimal("21600")).build();
        when(reportService.getYearlyReport(anyString(), eq(2024))).thenReturn(report);

        ResponseEntity<YearlyReportResponse> response = reportController.getYearlyReport(userDetails, 2024);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getYear()).isEqualTo(2024);
        assertThat(response.getBody().getNetSavings()).isEqualByComparingTo(new BigDecimal("21600"));
    }
}