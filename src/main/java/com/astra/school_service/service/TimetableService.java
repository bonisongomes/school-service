package com.astra.school_service.service;

import com.astra.school_service.dto.Period;
import com.astra.school_service.dto.Timetable;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class TimetableService {


    public Timetable getTimetable(String standard, String division) {
        Map<String, List<Period>> schedule = new LinkedHashMap<>();


        schedule.put("Monday", List.of(
                new Period("Maths", "Mr. Sharma", "09:00 - 09:30"),
                new Period("Science", "Mrs. Fernandes", "09:30 - 10:00"),
                new Period("English", "Ms. D'Souza", "10:15 - 10:45"),
                new Period("History", "Mr. Patil", "10:45 - 11:15")
        ));


        schedule.put("Tuesday", List.of(
                new Period("Science", "Mrs. Fernandes", "09:00 - 09:30"),
                new Period("Maths", "Mr. Sharma", "09:30 - 10:00"),
                new Period("Geography", "Mr. Khan", "10:15 - 10:45"),
                new Period("English", "Ms. D'Souza", "10:45 - 11:15")
        ));


        return new Timetable(standard, division, schedule);

    }
}
