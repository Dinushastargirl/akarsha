package com.akarsha.reports.dto;

import java.time.LocalDate;
import java.util.List;

public record AppointmentReportDTO(
    List<AppointmentByDate> appointmentsByDate
) {
    public record AppointmentByDate(
        LocalDate date,
        long total,
        long completed,
        long cancelled,
        long noShow
    ) {}
}
