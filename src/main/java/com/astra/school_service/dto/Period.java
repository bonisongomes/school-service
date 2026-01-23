package com.astra.school_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Period {
    private String subject;
    private String teacher;
    private String time;
}