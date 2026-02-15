package com.astra.school_service.model;

import lombok.Getter;

import java.util.List;
import java.util.Set;

public class Teacher {

    @Getter
    private final String name;
    private final Set<String> subjects;
    private final int maxWeeklyLoad;
    private final List<Availability> availability;
    @Getter
    private int currentLoad = 0;

    public Teacher(String name, Set<String> subjects,
                   int maxWeeklyLoad, List<Availability> availability) {
        this.name = name;
        this.subjects = subjects;
        this.maxWeeklyLoad = maxWeeklyLoad;
        this.availability = availability;
    }

    public boolean canTeach(String subject) {
        return subjects.contains(subject);
    }

    public Set<String> subjects() { return subjects; }

    public boolean isAvailable(TimeSlot slot) {
        return availability.stream().anyMatch(a ->
                a.day().equals(slot.day()) &&
                        slot.periodNo() >= a.startPeriod() &&
                        slot.periodNo() <= a.endPeriod()
        );
    }

    public boolean hasCapacity() {
        return currentLoad < maxWeeklyLoad;
    }

    public void assign() {
        currentLoad++;
    }

    public void decrementLoad() {
        if (currentLoad > 0) {
            currentLoad--;
        }
    }
}
