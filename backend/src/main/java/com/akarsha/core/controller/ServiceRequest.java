package com.akarsha.core.controller;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ServiceRequest {
    private String name;
    private BigDecimal price;
    private int durationMinutes;
    private boolean active = true;
}
