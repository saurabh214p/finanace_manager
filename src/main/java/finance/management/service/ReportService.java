package finance.management.service;


import finance.management.dto.response.MonthlyReportResponse;
import finance.management.dto.response.YearlyReportResponse;

/**
 * Service interface for financial report generation.
 */
public interface ReportService {

    MonthlyReportResponse getMonthlyReport(String username, int year, int month);

    YearlyReportResponse getYearlyReport(String username, int year);
}

