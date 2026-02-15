package com.astra.school_service.service;

import com.astra.school_service.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimetableStoreService {

    private final List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
    private final int periodsPerDay = 6;

    // in-memory schedules
    // class -> (slot -> entry)
    private final Map<ClassSection, Map<TimeSlot, TimetableEntry>> scheduleMap = new LinkedHashMap<>();

    // teacher name -> Teacher object (shared pool)
    private final Map<String, Teacher> teachers = new LinkedHashMap<>();

    // teacher name -> occupied slots (for quick conflict checks)
    private final Map<String, Set<TimeSlot>> teacherSchedule = new HashMap<>();

    public TimetableStoreService() {
        initTeachers();
        initClassesAndDummyTimetable();
    }

    private void initTeachers() {
        // create one teacher per subject (availability full day); maxWeeklyLoad large enough for dummy
        teachers.put("Ms. D'Souza", new Teacher("Ms. D'Souza", Set.of("English"), 30,
                List.of(new Availability("Monday", 1, 6), new Availability("Tuesday", 1, 6),
                        new Availability("Wednesday", 1, 6), new Availability("Thursday", 1, 6),
                        new Availability("Friday", 1, 6))));
        teachers.put("Mr. Kulkarni", new Teacher("Mr. Kulkarni", Set.of("Marathi"), 30,
                List.of(new Availability("Monday", 1, 6), new Availability("Tuesday", 1, 6),
                        new Availability("Wednesday", 1, 6), new Availability("Thursday", 1, 6),
                        new Availability("Friday", 1, 6))));
        teachers.put("Ms. Patil", new Teacher("Ms. Patil", Set.of("Hindi"), 30,
                List.of(new Availability("Monday", 1, 6), new Availability("Tuesday", 1, 6),
                        new Availability("Wednesday", 1, 6), new Availability("Thursday", 1, 6),
                        new Availability("Friday", 1, 6))));
        teachers.put("Mrs. Fernandes", new Teacher("Mrs. Fernandes", Set.of("Science"), 30,
                List.of(new Availability("Monday", 1, 6), new Availability("Tuesday", 1, 6),
                        new Availability("Wednesday", 1, 6), new Availability("Thursday", 1, 6),
                        new Availability("Friday", 1, 6))));
        teachers.put("Mr. Sharma", new Teacher("Mr. Sharma", Set.of("Maths"), 30,
                List.of(new Availability("Monday", 1, 6), new Availability("Tuesday", 1, 6),
                        new Availability("Wednesday", 1, 6), new Availability("Thursday", 1, 6),
                        new Availability("Friday", 1, 6))));
        teachers.put("Mr. Puri", new Teacher("Mr. Puri", Set.of("Social Science"), 30,
                List.of(new Availability("Monday", 1, 6), new Availability("Tuesday", 1, 6),
                        new Availability("Wednesday", 1, 6), new Availability("Thursday", 1, 6),
                        new Availability("Friday", 1, 6))));
        teachers.put("Mr. Diar", new Teacher("Mr. Diar", Set.of("English"), 30,
                List.of(new Availability("Monday", 1, 6), new Availability("Tuesday", 1, 6),
                        new Availability("Wednesday", 1, 6), new Availability("Thursday", 1, 6),
                        new Availability("Friday", 1, 6))));

        // initialize teacherSchedule entries
        for (String name : teachers.keySet()) teacherSchedule.put(name, new HashSet<>());
    }

    private void initClassesAndDummyTimetable() {
        List<ClassSection> classes = List.of(
                new ClassSection("5", "A"),
                new ClassSection("5", "B"),
                new ClassSection("6", "A"),
                new ClassSection("6", "B"),
                new ClassSection("7", "A"),
                new ClassSection("7", "B")
        );

        // subjects in fixed order
        List<String> subjects = List.of("English", "Marathi", "Hindi", "Science", "Maths", "Social Science");
        // map each subject to a teacher name
        Map<String, String> subjectToTeacher = Map.of(
                "English", "Ms. D'Souza",
                "Marathi", "Mr. Kulkarni",
                "Hindi", "Ms. Patil",
                "Science", "Mrs. Fernandes",
                "Maths", "Mr. Sharma",
                "Social Science", "Mr. Puri"
        );

        // prepare empty maps
        for (ClassSection cs : classes) scheduleMap.put(cs, new LinkedHashMap<>());

        // Fill timetable using a Latin-square style assignment so that at any timeslot across classes
        // every class has a different subject -> prevents teacher double-booking for initial dummy.
        for (String day : days) {
            for (int period = 1; period <= periodsPerDay; period++) {
                TimeSlot slot = new TimeSlot(day, period);
                for (int i = 0; i < classes.size(); i++) {
                    ClassSection cs = classes.get(i);
                    int subjIdx = ((period - 1) + i) % subjects.size();
                    String subject = subjects.get(subjIdx);
                    String teacherName = subjectToTeacher.get(subject);
                    // assign
                    TimetableEntry entry = new TimetableEntry(cs, subject, teacherName, slot);
                    scheduleMap.get(cs).put(slot, entry);
                    // mark teacher occupied for that slot and increment load
                    teacherSchedule.computeIfAbsent(teacherName, k -> new HashSet<>()).add(slot);
                    Teacher t = teachers.get(teacherName);
                    if (t != null) t.assign();
                }
            }
        }
    }

    public List<TimetableEntry> getAllEntries() {
        return scheduleMap.values().stream()
                .flatMap(m -> m.values().stream())
                .collect(Collectors.toList());
    }

    public List<TimetableEntry> getClassTimetable(String standard, String division) {
        ClassSection cs = new ClassSection(standard, division);
        Map<TimeSlot, TimetableEntry> m = scheduleMap.get(cs);
        if (m == null) return List.of();
        return new ArrayList<>(m.values());
    }

    public TimetableEntry getEntry(String standard, String division, String day, int period) {
        ClassSection cs = new ClassSection(standard, division);
        Map<TimeSlot, TimetableEntry> m = scheduleMap.get(cs);
        if (m == null) return null;
        return m.get(new TimeSlot(day, period));
    }


    public synchronized List<TimetableEntry> swapLectureAndTeacher(SwapRequest req) {
        // identify the two entries
        TimetableEntry a = getEntry(req.getStandardA(), req.getDivisionA(), req.getDayA(), req.getPeriodA());
        TimetableEntry b = getEntry(req.getStandardB(), req.getDivisionB(), req.getDayB(), req.getPeriodB());
        if (a == null || b == null) throw new AllocationException("One or both slots not found for swap");

        // teachers to swap
        String ta = a.teacher();
        String tb = b.teacher();

        // Validate teachers can teach the target subject at the other slot and are available
        Teacher teacherA = teachers.get(ta);
        Teacher teacherB = teachers.get(tb);
        if (teacherA == null || teacherB == null) throw new AllocationException("Teacher not found for swap");

        // teacherA should be able to teach b.subject at slot B
        TimeSlot slotA = a.slot();
        TimeSlot slotB = b.slot();
        String subjA = a.subject();
        String subjB = b.subject();

        //empty slotA and slotB since we are swapping teachers, we need to check availability and qualifications without the current assignment
        teacherSchedule.getOrDefault(ta, new HashSet<>()).remove(slotA);
        teacherSchedule.getOrDefault(tb, new HashSet<>()).remove(slotB);


        //verify if teacherA can teach subject A at slot B
        if (!teacherA.canTeach(subjA) || !teacherA.isAvailable(slotB) || isTeacherOccupied(teacherA.getName(),slotB)) {
            // restore schedules before throwing
            teacherSchedule.getOrDefault(ta, new HashSet<>()).add(slotA);
            throw new AllocationException("Swap invalid: Teacher qualifications or availability conflict");
        }
        //verify if teacherB can teach subject B at slot A
        if (!teacherB.canTeach(subjB) || !teacherB.isAvailable(slotA) || isTeacherOccupied(teacherB.getName(),slotA)) {
            // restore schedules before throwing
            teacherSchedule.getOrDefault(tb, new HashSet<>()).add(slotB);
            throw new AllocationException("Swap invalid: Teacher qualifications or availability conflict");
        }

        // perform swap
        // update teacher schedules
        teacherSchedule.computeIfAbsent(ta, k -> new HashSet<>()).add(slotB);
        teacherSchedule.computeIfAbsent(tb, k -> new HashSet<>()).add(slotA);
        // update timetable entries
        ClassSection csA = a.classSection();
        ClassSection csB = b.classSection();
        TimeSlot newSlotA = new TimeSlot(req.getDayB(), req.getPeriodB());
        TimeSlot newSlotB = new TimeSlot(req.getDayA(), req.getPeriodA());
        TimetableEntry newA = new TimetableEntry(csA, subjB, tb, newSlotB);
        TimetableEntry newB = new TimetableEntry(csB, subjA, ta, newSlotA);
        scheduleMap.get(csA).put(newSlotA, newA);
        scheduleMap.get(csB).put(newSlotB, newB);
         // return the updated entries for both slots
            return List.of(newA, newB);

    }

    //method to check if teacher is occupied at a given slot (used for validation in swap)
    public boolean isTeacherOccupied(String teacherName, TimeSlot slot) {
        Set<TimeSlot> occupied = teacherSchedule.getOrDefault(teacherName, Collections.emptySet());
        return occupied.contains(slot);
    }

    //method to assign teacher for a given slot , validate teacher can teach subject, is available and not occupied
    public synchronized TimetableEntry assignTeacher(ModifyRequest req) {
        ClassSection cs = new ClassSection(req.getStandard(), req.getDivision());
        TimeSlot slot = new TimeSlot(req.getDay(), req.getPeriod());
        Map<TimeSlot, TimetableEntry> classMap = scheduleMap.get(cs);

        if (req.getPeriod()< 1 || req.getPeriod()> 6) throw new AllocationException("Not a valid slot " + slot);
        if (!days.contains(req.getDay())) throw new AllocationException("Not a valid day " + slot);


        //verify teacher can teach the subject and is available and not occupied
        String teacher = req.getTeacher();
        Teacher teacherObj = teachers.get(teacher);
        if (teacherObj == null) throw new AllocationException("Teacher not found: " + teacher);
        if (!teacherObj.isAvailable(slot)) throw new AllocationException("Teacher " + teacher + " not available at " + slot);
        if (isTeacherOccupied(teacher, slot)) throw new AllocationException("Teacher " + teacher + " already occupied at " + slot);

        // I have timeslots and timetable entries in classMap , to check if incoming request slot is already present in classMap
        if (classMap != null && classMap.containsKey(slot)) {
            TimetableEntry existingTimeTableEntry = classMap.get(slot);
            String existingTeacher = existingTimeTableEntry.teacher();
            teacherSchedule.get(existingTeacher).remove(slot);
            Teacher oldTeacher = teachers.get(existingTeacher);
            if (oldTeacher != null) {
                    oldTeacher.decrementLoad();
                }
            scheduleMap.get(cs).remove(slot);
        }


        //update timetable entry with  teacher assignment and update teacher schedule
        TimetableEntry entry = new TimetableEntry(cs, req.getSubject(), teacher, slot);
        // mark teacher occupied for that slot and increment load
        teacherSchedule.computeIfAbsent(teacher, k -> new HashSet<>()).add(slot);
        teacherObj.assign();
        scheduleMap.computeIfAbsent(cs, k -> new LinkedHashMap<>()).put(slot, entry);
        return entry;
    }


}
