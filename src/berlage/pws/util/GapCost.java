package berlage.pws.util;

import berlage.pws.model.*;

/**
 * Gap = one empty period between a student's first and last lesson of a day.
 *
 * A hole is "dead" once none of the student's remaining groups has a free teacher in it.
 * Dead holes can never be filled, so they are final gaps (charged in g, never refunded).
 * Live holes that the remaining lessons cannot all fill give a lower bound on future dead holes (h).
 *
 * Incremental: per parent the occupancy is loaded once, stats are cached lazily per student,
 * and per child only students whose stats can actually change are recomputed.
 */
public final class GapCost {
    private static final int SHIFT = 16;
    private static final int MASK = 0xFFFF;

    private final Occupancy occupancy;
    private final SubjectGroup[][] groupsOfStudent;
    private final int[][] groupsLeft;
    private final int days;

    private final int[] gaps;
    private final int[] fillable;
    private final int[] touched;
    private final int[] parentStats;
    private final int[] stamp;

    private int currentStamp;
    private int parentIndex;
    private int deadDelta;
    private int boundDelta;

    public GapCost(Occupancy occupancy, SubjectGroup[][] groupsOfStudent, int[][] groupsLeft,
                   int days, int studentCount) {
        this.occupancy = occupancy;
        this.groupsOfStudent = groupsOfStudent;
        this.groupsLeft = groupsLeft;
        this.days = days;

        gaps = new int[days];
        fillable = new int[days];
        touched = new int[studentCount];
        parentStats = new int[studentCount];
        stamp = new int[studentCount];
    }

    /** Call after the parent's occupancy is loaded, before evaluating its children. */
    public void startParent(int index) {
        parentIndex = index;
        currentStamp++;
    }

    /** Evaluates placing the lesson on the parent. Results via deadDelta() and boundDelta(). */
    public void evaluate(ScheduledLesson lesson, int[] teacherStudents) {
        SubjectGroup group = lesson.getGroup();
        Slot slot = lesson.getSlot();
        int day = slot.getDay();
        int bit = 1 << slot.getPeriod();
        int count = 0;

        // Affected: the group's students, plus the teacher's other students who have a hole in this slot.
        for (int id : teacherStudents) {
            if (group.isStudentInGroup(id) || (occupancy.getGapBits(id, day) & bit) != 0) {
                touched[count++] = id;

                if (stamp[id] != currentStamp) {
                    stamp[id] = currentStamp;
                    parentStats[id] = stats(id, parentIndex);
                }
            }
        }

        occupancy.updateOccupancy(lesson, true);
        deadDelta = 0;
        boundDelta = 0;

        for (int i = 0; i < count; i++) {
            int id = touched[i];
            int before = parentStats[id];
            int after = stats(id, parentIndex + 1);

            deadDelta += (after >>> SHIFT) - (before >>> SHIFT);
            boundDelta += (after & MASK) - (before & MASK);
        }

        occupancy.updateOccupancy(lesson, false);
    }

    public int deadDelta() {
        return deadDelta;
    }

    public int boundDelta() {
        return boundDelta;
    }

    /** Packed: dead holes in the high bits, lower bound of future dead holes in the low bits. */
    private int stats(int id, int index) {
        int holes = 0;

        for (int d = 0; d < days; d++) {
            gaps[d] = occupancy.getGapBits(id, d);
            fillable[d] = 0;
            holes += Integer.bitCount(gaps[d]);
        }

        if (holes == 0) {
            return 0;
        }

        int capacity = 0;

        for (SubjectGroup group : groupsOfStudent[id]) {
            int left = groupsLeft[group.getId()][index];

            if (left == 0) {
                continue;
            }

            Teacher teacher = group.getTeacher();
            int reach = 0;

            for (int d = 0; d < days; d++) {
                if (gaps[d] == 0) {
                    continue;
                }

                int free = occupancy.getAvailablePeriods(teacher, d) & gaps[d];
                fillable[d] |= free;
                reach += Integer.bitCount(free);
            }

            capacity += Math.min(left, reach);
        }

        int live = 0;

        for (int d = 0; d < days; d++) {
            live += Integer.bitCount(fillable[d]);
        }

        return ((holes - live) << SHIFT) | Math.max(0, live - capacity);
    }
}