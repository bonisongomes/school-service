package com.astra.school_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/school_home")
    public String schoolPage() {
        return "landing";
    }
    @GetMapping("/timetable")
    public String timetablePage() {
        return "timetable";
    }
}

