package com.astra.school_service.service;

import com.astra.school_service.model.TimetableEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimetableGenerationService {

    private final TimetableStoreService storeService;

    public TimetableGenerationService(TimetableStoreService storeService) {
        this.storeService = storeService;
    }

    public List<TimetableEntry> generateFullSchoolTimetable() {
        // Previously this method used AdvancedTimetableAllocator. That allocator has been removed
        // in favor of the in-memory TimetableStoreService which holds a dummy timetable.
        return storeService.getAllEntries();
    }

    public List<TimetableEntry> getClassTimetable(
            String standard, String division, List<TimetableEntry> all) {
        List<TimetableEntry> timetableEntries = all.stream()
                .filter(e -> e.classSection().standard().equals(standard))
                .filter(e -> e.classSection().division().equals(division))
                .toList();
        System.out.println("Filtered Timetable for Class " + standard + division + " : " + timetableEntries);

        return timetableEntries;
    }
}
