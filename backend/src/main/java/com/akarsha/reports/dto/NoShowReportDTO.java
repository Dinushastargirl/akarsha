package com.akarsha.reports.dto;

import java.util.List;

public record NoShowReportDTO(
    long totalNoShows,
    double noShowRate,
    List<NoShowByService> noShowsByService,
    List<NoShowByStaff> noShowsByStaff,
    List<NoShowByDate> noShowsByDate
) {
    public record NoShowByService(Long serviceId, String serviceName, long count) {}
    public record NoShowByStaff(Long staffId, String staffName, long count) {}
    public record NoShowByDate(String date, long count) {}
}
