package berlage.pws.util;

import berlage.pws.model.*;

import java.util.Arrays;
import java.util.List;

public final class CostManager {
    private static final int SPAN_WEIGHT = 0;
    private static final int SPREAD_WEIGHT = 0;
    public static final int GAP_WEIGHT = 10;

    private CostManager() {}

    public static int getTheoreticalBest(List<SubjectGroup> lessonsInstances) {
        int total = 0;

        for (SubjectGroup group : lessonsInstances) {
            int studentCount = group.getStudentsSet().size();
            total += GAP_WEIGHT * studentCount;
        }

        return total;
    }

    public static int calculateFullHCost(int[] students,
                                         int[][] remainingLessonTable,
                                         int[] studentGapCount,
                                         int index) {
        int cost = 0;

        for (int id : students) {
            int lessonsLeft = remainingLessonTable[id][index];
            cost += Math.max(0, lessonsLeft - studentGapCount[id]) * GAP_WEIGHT;
        }

        System.out.println("ROOT ESTIMATE: " + cost);
        return cost;
    }


    public static int countFillableGaps(int dayBits, int fillableRow) {
        if (dayBits == 0) {
            return 0;
        }

        int first = Integer.numberOfTrailingZeros(dayBits);
        int last = 31 - Integer.numberOfLeadingZeros(dayBits);
        int span = (-1 << first) & (-1 >>> (31 - last));
        int gaps = span & ~dayBits;

        return Integer.bitCount(gaps & fillableRow);
    }

    public static int getFillableGapCount(Occupancy occupancy, int[] fillableRows, int id, int days) {
        int count = 0;

        for (int day = 0; day < days; day++) {
            count += countFillableGaps(occupancy.getStudentDayBits(id, day), fillableRows[day]);
        }

        return count;
    }

    public static int calculateHCost(Occupancy occupancy,
                                     ScheduledLesson lesson,
                                     int[][] remainingLessonTable,
                                     int[][][] fillableTable,
                                     int[] studentFillableCount,
                                     int parentIndex,
                                     int parentHCost,
                                     int oldGroupContribution,
                                     int weight,
                                     int days) {
        int placedDay = lesson.getSlot().getDay();
        int placedBit = 1 << lesson.getSlot().getPeriod();
        int childIndex = parentIndex + 1;
        int newHCost = 0;

        for (int id : lesson.getGroup().getStudents()) {
            int[] parentRows = fillableTable[id][parentIndex];
            int[] childRows = fillableTable[id][childIndex];
            int fillable;

            if (Arrays.equals(parentRows, childRows)) {
                int bits = occupancy.getStudentDayBits(id, placedDay);
                fillable = studentFillableCount[id]
                           - countFillableGaps(bits, parentRows[placedDay])
                           + countFillableGaps(bits | placedBit, parentRows[placedDay]);
            } else {
                fillable = 0;

                for (int day = 0; day < days; day++) {
                    int bits = occupancy.getStudentDayBits(id, day);

                    if (day == placedDay) {
                        bits |= placedBit;
                    }

                    fillable += countFillableGaps(bits, childRows[day]);
                }
            }

            newHCost += Math.max(0, remainingLessonTable[id][childIndex] - fillable) * GAP_WEIGHT;
        }

        return parentHCost + (newHCost - oldGroupContribution) * weight;
    }

    public static int calculateGCost(Occupancy occupancy,
                                     ScheduledLesson lesson) {


        SubjectGroup group = lesson.getGroup();
        Slot slot = lesson.getSlot();
        int newPeriod = slot.getPeriod();
        int dayIndex = slot.getDay();
        int cost = 0;

        for (int id : group.getStudents()) {
            int lessonCount = occupancy.getLessonCountOfStudentOnDay(id, dayIndex);

            if (lessonCount == 0) {
                cost += GAP_WEIGHT;
                continue;
            }

            int firstPeriod = occupancy.getFirstOccupiedPeriod(id, dayIndex);
            int lastPeriod = occupancy.getLastOccupiedPeriod(id, dayIndex);

            int newFirst = Math.min(firstPeriod, newPeriod);
            int newLast = Math.max(lastPeriod, newPeriod);

            int spanDelta = ((newLast - newFirst) - (lastPeriod - firstPeriod));
            int gapCost = 0;

            if (spanDelta == 0) {
                gapCost = 0;
            } else {
                gapCost += Math.max(firstPeriod - newFirst, newLast - lastPeriod);
            }

            int weightedGapCost = gapCost * GAP_WEIGHT;

            cost += weightedGapCost;
        }

        return cost;
    }

    public static int getOldContribution(SubjectGroup group, int[][] remainingLessonTable, int[] studentGapCount, int index) {
        int cost = 0;

        for (int id : group.getStudents()) {
            int lessonsLeft = remainingLessonTable[id][index];
            cost += Math.max(0, lessonsLeft - studentGapCount[id]) * GAP_WEIGHT;
        }

        return cost;
    }
}
