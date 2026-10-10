package berlage.pws.util;

import berlage.pws.model.*;

import java.util.Arrays;

public class Occupancy {
    private final boolean[][] teacherOccupied;
    private final boolean[][] roomOccupied;
    private final boolean[][] studentOccupied;
    private final int[][] countAssigned;
    private final int[][] studentDayCount;
    private final int maxCountPerDay;
    private final int lessonsPerDay;

    public Occupancy(int teacherCount, int roomCount, int studentCount, int lessonsPerDay, int days, int maxCountPerDay, int groupCount) {
        teacherOccupied = new boolean[teacherCount][days * lessonsPerDay];
        roomOccupied = new boolean[roomCount][days * lessonsPerDay];
        studentOccupied = new boolean[studentCount][days * lessonsPerDay];
        countAssigned = new int[groupCount][days];
        studentDayCount = new int[studentCount][days];

        this.maxCountPerDay = maxCountPerDay;
        this.lessonsPerDay = lessonsPerDay;
    }

    public int getLessonCountOfStudentOnDay(Student student, int day) {
        return studentDayCount[student.getId()][day];
    }

    public int getFirstOccupiedPeriod(Student student, int day) {
        boolean[] studentSlots = studentOccupied[student.getId()];
        int startingIndex = day * lessonsPerDay;
        int endingIndex = day * lessonsPerDay + lessonsPerDay;

        for (int i = startingIndex; i < endingIndex; i++) {
            if (studentSlots[i]) {
                return i - startingIndex;
            }
        }

        throw new IllegalStateException("OCCUPANCY FAIL: empty day check fail (FIRST)");
    }

    public int getLastOccupiedPeriod(Student student, int day) {
        boolean[] studentSlots = studentOccupied[student.getId()];
        int startingIndex = day * lessonsPerDay;
        int endingIndex = day * lessonsPerDay + lessonsPerDay;

        for (int i = endingIndex - 1; i >= startingIndex; i--) {
            if (studentSlots[i]) {
                return i - startingIndex;
            }
        }

        throw new IllegalStateException("OCCUPANCY FAIL: empty day check fail (LAST)");
    }

    private boolean isTeacherBusy(Teacher teacher, Slot slot) {
        return teacherOccupied[teacher.getId()][slot.getFlatIndex()];
    }

    public boolean isRoomBusy(Room room, Slot slot) {
        return roomOccupied[room.getId()][slot.getFlatIndex()];
    }

    private boolean isStudentBusy(Student student, Slot slot) {
        return studentOccupied[student.getId()][slot.getFlatIndex()];
    }

    public boolean canPlace(SubjectGroup group, Slot slot) {
        Teacher teacher = group.getTeacher();

        if (!teacher.isAvailable(slot.getDay(), slot.getPeriod())) {
            return false;
        }

        if (countAssigned[group.getId()][slot.getDay()] >= maxCountPerDay) {
            return false;
        }

        if (isTeacherBusy(teacher, slot)) {
            return false;
        }

        for (Student student : group.getStudents()) {
            if (isStudentBusy(student, slot)) {
                return false;
            }
        }

        return true;
    }

    public void buildFullState(ScheduleState state) {
        clear();
        ScheduleState temp = state;

        while (temp != null && temp.getPlacedLesson() != null) {
            ScheduledLesson lesson = temp.getPlacedLesson();
            SubjectGroup group = lesson.getGroup();
            int flatIndex = lesson.getSlot().getFlatIndex();
            int dayIndex = lesson.getSlot().getDay();

            roomOccupied[lesson.getRoom().getId()][flatIndex] = true;
            teacherOccupied[group.getTeacher().getId()][flatIndex] = true;
            countAssigned[group.getId()][dayIndex]++;

            for (Student student : group.getStudents()) {
                studentOccupied[student.getId()][flatIndex] = true;
                studentDayCount[student.getId()][dayIndex]++;
            }

            temp = temp.getParent();
        }
    }

    private void clear() {
        for (int i = 0; i < teacherOccupied.length; i++) {
            Arrays.fill(teacherOccupied[i], false);
        }

        for (int i = 0; i < roomOccupied.length; i++) {
            Arrays.fill(roomOccupied[i], false);
        }

        for (int i = 0; i < studentOccupied.length; i++) {
            Arrays.fill(studentOccupied[i], false);
        }

        for (int i = 0; i < countAssigned.length; i++) {
            Arrays.fill(countAssigned[i], 0);
        }

        for (int i = 0; i < studentDayCount.length; i++) {
            Arrays.fill(studentDayCount[i], 0);
        }
    }
}
