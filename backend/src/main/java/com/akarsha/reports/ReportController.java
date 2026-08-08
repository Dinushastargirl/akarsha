package com.akarsha.reports;

import com.akarsha.reports.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ReportOverviewDTO> getOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "UTC") String tz) {
        validateDates(startDate, endDate);
        return ResponseEntity.ok(reportService.getOverview(startDate, endDate, tz));
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportDTO> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "UTC") String tz) {
        validateDates(startDate, endDate);
        return ResponseEntity.ok(reportService.getRevenueReport(startDate, endDate, tz));
    }

    @GetMapping("/appointments")
    public ResponseEntity<AppointmentReportDTO> getAppointmentsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        validateDates(startDate, endDate);
        return ResponseEntity.ok(reportService.getAppointmentsReport(startDate, endDate));
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServicePerformanceDTO>> getServicePerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        validateDates(startDate, endDate);
        return ResponseEntity.ok(reportService.getServicePerformance(startDate, endDate));
    }

    @GetMapping("/staff")
    public ResponseEntity<List<StaffPerformanceDTO>> getStaffPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        validateDates(startDate, endDate);
        return ResponseEntity.ok(reportService.getStaffPerformance(startDate, endDate));
    }

    @GetMapping("/customers")
    public ResponseEntity<CustomerInsightsDTO> getCustomerInsights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "UTC") String tz) {
        validateDates(startDate, endDate);
        return ResponseEntity.ok(reportService.getCustomerInsights(startDate, endDate, tz));
    }

    @GetMapping("/no-shows")
    public ResponseEntity<NoShowReportDTO> getNoShowsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        validateDates(startDate, endDate);
        return ResponseEntity.ok(reportService.getNoShowReport(startDate, endDate));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
