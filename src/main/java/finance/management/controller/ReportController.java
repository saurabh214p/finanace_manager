package finance.management.controller;

import finance.management.dto.response.MonthlyReportResponse;
import finance.management.dto.response.YearlyReportResponse;
import finance.management.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for financial report endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generates a monthly financial report for the specified year and month.
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        MonthlyReportResponse response = reportService.getMonthlyReport(
                userDetails.getUsername(), year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a yearly financial report for the specified year.
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year) {
        YearlyReportResponse response = reportService.getYearlyReport(
                userDetails.getUsername(), year);
        return ResponseEntity.ok(response);
    }
}
