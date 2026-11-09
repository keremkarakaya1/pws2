package berlage.pws.util;

import berlage.pws.model.*;

import java.util.Arrays;

public class Occupancy {
    private final int[][] teacherOccupied;
    private final int[][] roomOccupied;
    private final int[][] studentOccupied;
    private final int[][] countAssigned;
    private final int maxCountPerDay;
    private ScheduleState loadedState;

    public Occupancy(int teacherCount, int roomCount, int studentCount, int lessonsPerDay, int days, int maxCountPerDay, int groupCount) {
        teacherOccupied = new int[teacherCount][days];
        roomOccupied = new int[roomCount][days];
        studentOccupied = new int[studentCount][days];
        countAssigned = new int[groupCount][days];

        loadedState = null;
        this.maxCountPerDay = maxCountPerDay;
    }

    public int getLessonCountOfStudentOnDay(int id, int day) {
        return Integer.bitCount(studentOccupied[id][day]);
    }

    public int getStudentScheduleOnDay(int id, int day) {
        int dayBits = studentOccupied[id][day];

        if (dayBits == 0) {
            throw new IllegalStateException("EMPTY DAY");
        }

        return dayBits;
    }

    public void updateOccupancy(ScheduledLesson lesson, boolean place) {
        SubjectGroup group = lesson.getGroup();
        Teacher teacher = group.getTeacher();
        Room room = lesson.getRoom();
        Slot slot = lesson.getSlot();
        int flatIndex = slot.getFlatIndex();
        int period = slot.getPeriod();
        int day = slot.getDay();
        int change = place ? 1 : -1;
        int bitChange = 1 << period;

        countAssigned[group.getId()][day] += change;

        if (place) {
            teacherOccupied[teacher.getId()][day] |= bitChange;
            roomOccupied[room.getId()][day] |= bitChange;
        } else {
            teacherOccupied[teacher.getId()][day] &= ~bitChange;
            roomOccupied[room.getId()][day] &= ~bitChange;
        }

        if (place) {
            for (int id : group.getStudents()) {
                studentOccupied[id][day] |= bitChange;
            }
        } else {
            for (int id : group.getStudents()) {
                studentOccupied[id][day] &= ~bitChange;
            }
        }
    }

    public int getFirstOccupiedPeriod(int id, int day) {
        int dayBits =  studentOccupied[id][day];

        if (dayBits == 0) {
            throw new IllegalStateException("OCCUPANCY FAIL: empty day check fail (FIRST)");
        }

        int countTrailingZeros = Integer.numberOfTrailingZeros(dayBits);
        return countTrailingZeros;
    }

    public int getLastOccupiedPeriod(int id, int day) {
        int dayBits =  studentOccupied[id][day];

        if (dayBits == 0) {
            throw new IllegalStateException("OCCUPANCY FAIL: empty day check fail (LAST)");
        }

        int countLeadingZeros = Integer.numberOfLeadingZeros(dayBits);
        return 31 - countLeadingZeros;
    }

    public boolean isTeacherBusy(Teacher teacher, int day, int index) {
        int dayBits = teacherOccupied[teacher.getId()][day];
        return (dayBits & (1 << index)) != 0;
    }

    public boolean isTeacherBusy(Teacher teacher, Slot slot) {
        int dayBits = teacherOccupied[teacher.getId()][slot.getDay()];
        return (dayBits & (1 << slot.getPeriod())) != 0;
    }

    public boolean isRoomBusy(Room room, Slot slot) {
        int dayBits = roomOccupied[room.getId()][slot.getDay()];
        return (dayBits & (1 << slot.getPeriod())) != 0;
    }

    public boolean isStudentBusy(int id, Slot slot) {
        int dayBits = studentOccupied[id][slot.getDay()];
        return (dayBits & (1 << slot.getPeriod())) != 0;
    }

    public boolean isStudentBusy(int id, int day, int period) {
        int dayBits = studentOccupied[id][day];
        return (dayBits & (1 << period)) != 0;
    }

    public int getStudentDayBits(int id, int day) {
        return studentOccupied[id][day];
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

        for (int id : group.getStudents()) {
            if (isStudentBusy(id, slot)) {
                return false;
            }
        }

        return true;
    }

    public void buildFullState(ScheduleState target) {
        ScheduleState movingPointer = target;

        if (loadedState == null) {
            while (movingPointer != null && movingPointer.getPlacedLesson() != null) {
                updateOccupancy(movingPointer.getPlacedLesson(), true);
                movingPointer = movingPointer.getParent();
            }

            loadedState = target;
            return;
        }

        while (movingPointer.getIndex() != loadedState.getIndex()) {
            if (loadedState.getIndex() < movingPointer.getIndex()) {
                movingPointer = movingPointer.getParent();
            } else {
                updateOccupancy(loadedState.getPlacedLesson(), false);
                loadedState = loadedState.getParent();
            }
        }

        while (movingPointer != loadedState) {
            updateOccupancy(loadedState.getPlacedLesson(), false);
            movingPointer = movingPointer.getParent();
            loadedState = loadedState.getParent();
        }

        movingPointer = target;

        while (movingPointer != loadedState) {
            updateOccupancy(movingPointer.getPlacedLesson(), true);
            movingPointer = movingPointer.getParent();
        }

        loadedState = target;
    }

    public void reset() {
        clear();
        loadedState = null;
    }

    private void clear() {
        for (int i = 0; i < teacherOccupied.length; i++) {
            Arrays.fill(teacherOccupied[i], 0);
        }

        for (int i = 0; i < roomOccupied.length; i++) {
            Arrays.fill(roomOccupied[i], 0);
        }

        for (int i = 0; i < studentOccupied.length; i++) {
            Arrays.fill(studentOccupied[i], 0);
        }

        for (int i = 0; i < countAssigned.length; i++) {
            Arrays.fill(countAssigned[i], 0);
        }
    }
}
