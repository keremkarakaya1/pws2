package berlage.pws.impl;

import berlage.pws.model.*;

import java.util.Random;
import java.util.Set;

public class ZobristHashing {
    private final long[][] teacherSlot;
    private final long[][] roomSlot;
    private final long[][] studentSlot;
    private final long[][] subjectGroupSlot;
    private final int lessonsPerDay;
    private final int slotCount;

    //TODO add per subjectgroup keying

    public ZobristHashing(int lessonsPerDay, int countDays, int countTeachers, int countRooms, int countStudents, int countGroups, long seed) {
        slotCount = lessonsPerDay * countDays;
        this.lessonsPerDay = lessonsPerDay;
        teacherSlot = new long[countTeachers][slotCount];
        roomSlot = new long[countRooms][slotCount];
        studentSlot = new long[countStudents][slotCount];
        subjectGroupSlot = new long[countGroups][slotCount];

        initTables(seed);
    }

    public long hashFor(ScheduledLesson lesson) {
        long delta = 0;

        SubjectGroup group = lesson.getGroup();
        Room room = lesson.getRoom();
        Slot slot = lesson.getSlot();
        Teacher teacher = group.getTeacher();
        Set<Student> students = group.getStudents();

        int slotIndex = slot.getFlatIndex();

        for (Student student : students) {
            delta ^= studentSlot[student.getId()][slotIndex];
        }

        delta ^= roomSlot[room.getId()][slotIndex];
        delta ^= teacherSlot[teacher.getId()][slotIndex];
        delta ^= subjectGroupSlot[group.getId()][slotIndex];

        return delta;
    }

    private void fillTable(long[][] table, Random random) {
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < slotCount; j++) {
                table[i][j] = random.nextLong();
            }
        }
    }

    private void initTables(long seed) {
        Random random = new Random(seed);

        fillTable(teacherSlot, random);
        fillTable(roomSlot, random);
        fillTable(studentSlot, random);
        fillTable(subjectGroupSlot, random);
    }

}
