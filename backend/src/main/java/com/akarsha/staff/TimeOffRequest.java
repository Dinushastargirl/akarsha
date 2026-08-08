package com.akarsha.staff;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TimeOffRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
}
