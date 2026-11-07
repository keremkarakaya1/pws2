package berlage.pws.util;

import berlage.pws.model.*;

import java.util.Arrays;

public class Occupancy {
    private final boolean[][] teacherOccupied;
    private final boolean[][] roomOccupied;
    private final int[][] studentOccupied;
    private final int[][] countAssigned;
    //private final int[][] studentDayCount;
    private final int maxCountPerDay;
    private final int lessonsPerDay;
    private ScheduleState loadedState;

    public Occupancy(int teacherCount, int roomCount, int studentCount, int lessonsPerDay, int days, int maxCountPerDay, int groupCount) {
        teacherOccupied = new boolean[teacherCount][days * lessonsPerDay];
        roomOccupied = new boolean[roomCount][days * lessonsPerDay];
        studentOccupied = new int[studentCount][days];
        countAssigned = new int[groupCount][days];
        //studentDayCount = new int[studentCount][days];

        loadedState = null;
        this.maxCountPerDay = maxCountPerDay;
        this.lessonsPerDay = lessonsPerDay;
    }

    public int getLessonCountOfStudentOnDay(int id, int day) {
        return Integer.bitCount(studentOccupied[id][day]);
    }

    public int getStudentScheduleOnDay(int id, int day) {
        if (getLessonCountOfStudentOnDay(id,day) == 0) {
            throw new IllegalStateException("EMPTY DAY");
        }

        int dayBits = studentOccupied[id][day];
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

        teacherOccupied[teacher.getId()][flatIndex] = place;
        roomOccupied[room.getId()][flatIndex] = place;
        countAssigned[group.getId()][day] += change;

        if (place) {
            for (int id : group.getStudents()) {
                studentOccupied[id][day] |= bitChange;
                //studentDayCount[id][day]++;
            }
        } else {
            for (int id : group.getStudents()) {
                studentOccupied[id][day] &= ~bitChange;
                //studentDayCount[id][day]--;
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
        return teacherOccupied[teacher.getId()][day * lessonsPerDay + index];
    }

    public boolean isTeacherBusy(Teacher teacher, Slot slot) {
        return teacherOccupied[teacher.getId()][slot.getFlatIndex()];
    }

    public boolean isRoomBusy(Room room, Slot slot) {
        return roomOccupied[room.getId()][slot.getFlatIndex()];
    }

    public boolean isStudentBusy(int id, Slot slot) {
        int dayBits = studentOccupied[id][slot.getDay()];
        return (dayBits & (1 << slot.getPeriod())) != 0;
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
            Arrays.fill(teacherOccupied[i], false);
        }

        for (int i = 0; i < roomOccupied.length; i++) {
            Arrays.fill(roomOccupied[i], false);
        }

        for (int i = 0; i < studentOccupied.length; i++) {
            Arrays.fill(studentOccupied[i], 0);
        }

        for (int i = 0; i < countAssigned.length; i++) {
            Arrays.fill(countAssigned[i], 0);
        }

        //for (int i = 0; i < studentDayCount.length; i++) {
          //  Arrays.fill(studentDayCount[i], 0);
       // }
    }
}
