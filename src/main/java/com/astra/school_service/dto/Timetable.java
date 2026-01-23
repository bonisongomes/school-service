package com.astra.school_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Timetable {
    private String standard;
    private String division;
    private Map<String, List<Period>> weeklySchedule;
}
