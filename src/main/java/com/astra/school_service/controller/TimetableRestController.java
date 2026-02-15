package com.astra.school_service.controller;

import com.astra.school_service.model.TimetableEntry;
import com.astra.school_service.service.SwapRequest;
import com.astra.school_service.service.ModifyRequest;
import com.astra.school_service.service.TimetableGenerationService;
import com.astra.school_service.service.TimetableStoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
public class TimetableRestController {

    private final TimetableGenerationService generationService;
    private final TimetableStoreService storeService;

    public TimetableRestController(TimetableGenerationService generationService, TimetableStoreService storeService) {
        this.generationService = generationService;
        this.storeService = storeService;
    }

    // 🔹 Generate full school timetable (existing behaviour - kept for compatibility)
    @PostMapping("/generate")
    public List<TimetableEntry> generate() {
        return generationService.generateFullSchoolTimetable();
    }

    // 🔹 Get timetable for ONE class (uses in-memory store)
    @GetMapping
    public List<TimetableEntry> getClassTimetable(
            @RequestParam String standard,
            @RequestParam String division) {

        return storeService.getClassTimetable(standard, division);
    }

    // 🔹 Get full school timetable (in-memory)
    @GetMapping("/all")
    public List<TimetableEntry> getFullTimetable() {
        return storeService.getAllEntries();
    }



    // 🔹 Swap teachers between two slots
    @PostMapping("/swap")
    public List<TimetableEntry> swapTeachers(@RequestBody SwapRequest req) {
        return storeService.swapLectureAndTeacher(req);
    }

    // Assign a teacher to a slot
    @PostMapping("/assign")
    public TimetableEntry assignTeacher(@RequestBody ModifyRequest req) {
        return storeService.assignTeacher(req);
    }
}
