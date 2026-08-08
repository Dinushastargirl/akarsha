package com.akarsha.staff;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRequest {
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean active = true;
}
