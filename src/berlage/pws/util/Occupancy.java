package berlage.pws.util;

import berlage.pws.model.*;

import java.util.Arrays;

public class Occupancy {
    private final boolean[][] teacherOccupied;
    private final boolean[][] roomOccupied;
    private final boolean[][] studentOccupied;
    private final int[][] countAssigned;
    private final int maxCountPerDay;

    public Occupancy(int teacherCount, int roomCount, int studentCount, int lessonsPerDay, int days, int maxCountPerDay, int groupCount) {
        teacherOccupied = new boolean[teacherCount][days * lessonsPerDay];
        roomOccupied = new boolean[roomCount][days * lessonsPerDay];
        studentOccupied = new boolean[studentCount][days * lessonsPerDay];
        countAssigned = new int[groupCount][days];

        this.maxCountPerDay = maxCountPerDay;
    }

    private boolean isTeacherBusy(int id, Slot slot) {
        return teacherOccupied[id][slot.getFlatIndex()];
    }

    public boolean isRoomBusy(int id, Slot slot) {
        return roomOccupied[id][slot.getFlatIndex()];
    }

    private boolean isStudentBusy(int id, Slot slot) {
        return studentOccupied[id][slot.getFlatIndex()];
    }

    public boolean canPlace(SubjectGroup group, Slot slot) {
        Teacher teacher = group.getTeacher();

        if (!teacher.isAvailable(slot.getDay(), slot.getPeriod())) {
            return false;
        }

        if (countAssigned[group.getId()][slot.getDay()] >= maxCountPerDay) {
            return false;
        }

        if (isTeacherBusy(teacher.getId(), slot)) {
            return false;
        }

        for (Student student : group.getStudents()) {
            if (isStudentBusy(student.getId(), slot)) {
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

            roomOccupied[lesson.getRoom().getId()][flatIndex] = true;
            teacherOccupied[group.getTeacher().getId()][flatIndex] = true;
            countAssigned[group.getId()][lesson.getSlot().getDay()]++;

            for (Student student : group.getStudents()) {
                studentOccupied[student.getId()][flatIndex] = true;
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
    }
}
