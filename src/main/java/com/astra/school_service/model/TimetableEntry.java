package com.astra.school_service.model;

public record TimetableEntry(
        ClassSection classSection,
        String subject,
        String teacher,
        TimeSlot slot
) {}

