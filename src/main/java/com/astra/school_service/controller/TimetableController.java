package com.astra.school_service.controller;

import com.astra.school_service.dto.Timetable;
import com.astra.school_service.service.TimetableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class TimetableController {


    private final TimetableService timetableService;


    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }


    @GetMapping("/")
    public String home() {
        return "index";
    }


    @GetMapping("/timetable")
    public String showTimetable(
            @RequestParam String standard,
            @RequestParam String division,
            Model model) {
        Timetable timetable = timetableService.getTimetable(standard, division);
        model.addAttribute("timetable", timetable);
        return "timetable";
    }
}
