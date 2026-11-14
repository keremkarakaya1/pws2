package berlage.pws.util;

import berlage.pws.model.*;

import java.util.List;

public final class CostManager {
    private static final int SPAN_WEIGHT = 0;
    private static final int SPREAD_WEIGHT = 0;
    public static final int GAP_WEIGHT = 10;
    public static final int SCALE_FACTOR = 100;

    private CostManager() {}

    public static int getTheoreticalBest(List<SubjectGroup> lessonsInstances) {
        int total = 0;

        for (SubjectGroup group : lessonsInstances) {
            int studentCount = group.getStudentsSet().size();
            total += GAP_WEIGHT * studentCount * SCALE_FACTOR;
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

    private static void updateStudentGapCounts(SubjectGroup group,
                                               Occupancy occupancy,
                                               int[] studentGapCounts,
                                               int dayIndex,
                                               int newPeriod,
                                               boolean positive) {
        for (int id : group.getStudents()) {
            int gapChange = 0;

            if (occupancy.getLessonCountOfStudentOnDay(id, dayIndex) != 0) {
                gapChange = getHeuristicGapChange(id, occupancy, newPeriod, dayIndex);
                studentGapCounts[id] += gapChange * (positive ? 1 : -1);
            }
        }
    }

    public static int calculateAdvancedHCost(Occupancy occupancy,
                                             ScheduledLesson lesson,
                                             SubjectGroup[][] subjectGroupsPerStudent,
                                             int[][] countGroupsToBeScheduledAtIndex,
                                             int[][] remainingLessonTable,
                                             int[] studentGapCount,
                                             int[] affectedStudents,
                                             int[] contributionPerStudent,
                                             int index,
                                             int days,
                                             int weight,
                                             int parentHCost) {



        SubjectGroup group = lesson.getGroup();
        Slot slot = lesson.getSlot();
        int oldContribution = 0;
        int dayIndex = slot.getDay();
        int newPeriod = slot.getPeriod();
        int cost = 0;

       updateStudentGapCounts(group, occupancy, studentGapCount, dayIndex, newPeriod, true);
       occupancy.updateOccupancy(lesson, true);

        for (int id : affectedStudents) {
            if (!group.isStudentInGroup(id)
                && (occupancy.getLessonCountOfStudentOnDay(id, dayIndex) == 0
                    || occupancy.isStudentBusy(id, slot)
                    || newPeriod < occupancy.getFirstOccupiedPeriod(id, dayIndex)
                    || newPeriod > occupancy.getLastOccupiedPeriod(id, dayIndex))) {
                continue;
            }

            oldContribution += contributionPerStudent[id];

            cost += calculateStudentForcedGapCost(occupancy,
                                                    id,
                                                    studentGapCount[id],
                                                    remainingLessonTable[id][index],
                                                    subjectGroupsPerStudent,
                                                    countGroupsToBeScheduledAtIndex,
                                                    index,
                                                    days);
        }

        occupancy.updateOccupancy(lesson, false);
        updateStudentGapCounts(group, occupancy, studentGapCount, dayIndex, newPeriod, false);

        return parentHCost + (cost - oldContribution) * weight;
    }

    public static void getOldAdvancedContribution(Occupancy occupancy,
                                                 int[] affectedStudents,
                                                 SubjectGroup[][] subjectGroupsPerStudent,
                                                 int[][] countGroupsToBeScheduledAtIndex,
                                                 int[][] remainingLessonTable,
                                                 int[] studentGapCount,
                                                 int[] contributionPerStudent,
                                                 int index,
                                                 int days) {

        for (int id : affectedStudents) {
            contributionPerStudent[id] = calculateStudentForcedGapCost(occupancy, id, studentGapCount[id],
                    remainingLessonTable[id][index], subjectGroupsPerStudent,
                    countGroupsToBeScheduledAtIndex, index, days);
        }
    }

    private static int calculateStudentForcedGapCost(Occupancy occupancy,
                                                    int id,
                                                    int gapCount,
                                                    int lessonsLeft,
                                                    SubjectGroup[][] subjectGroupsPerStudent,
                                                    int[][] countGroupsToBeScheduledAtIndex,
                                                    int index,
                                                    int days) {
        SubjectGroup[] groupsOfStudent = subjectGroupsPerStudent[id];
        int unfillable = 0;

        for (int i = 0; i < days; i++) {
            int gaps = occupancy.getGapBits(id, i);

            if (gaps == 0) {
                continue;
            }

            int fillable = 0;

            for (SubjectGroup subjectGroup : groupsOfStudent) {
                if (countGroupsToBeScheduledAtIndex[subjectGroup.getId()][index] <= 0) {
                    continue;
                }

                fillable |= occupancy.getAvailablePeriods(subjectGroup.getTeacher(), i) & gaps;

                if (fillable == gaps) {
                    break;
                }
            }
            
            unfillable += Integer.bitCount(gaps & ~fillable);
        }

        int forcedGapCount = Math.max(0, lessonsLeft - (gapCount - unfillable));
        return forcedGapCount * GAP_WEIGHT;
    }

    public static int calculateHCost(Occupancy occupancy,
                                     ScheduledLesson lesson,
                                     int[][] remainingLessonTable,
                                     int[] studentGapCount,
                                     int parentIndex,
                                     int parentHCost,
                                     int oldGroupContribution,
                                     int weight) {


        SubjectGroup group = lesson.getGroup();
        Slot slot = lesson.getSlot();
        int dayIndex = slot.getDay();
        int newPeriod = slot.getPeriod();
        int childIndex = parentIndex + 1;
        int newHCost = 0;

        for (int id : group.getStudents()) {
            int gapCountBefore = studentGapCount[id];
            int gapChange = 0;


            if (occupancy.getLessonCountOfStudentOnDay(id, dayIndex) != 0) {
                gapChange = getHeuristicGapChange(id, occupancy, newPeriod, dayIndex);
            }

            int lessonsLeft = remainingLessonTable[id][childIndex];
            int countLessonsForcedToExtendDay = Math.max(0, lessonsLeft - (gapCountBefore + gapChange));

            newHCost += countLessonsForcedToExtendDay * GAP_WEIGHT;
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
                cost += GAP_WEIGHT * SCALE_FACTOR;
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

            int weightedGapCost = gapCost * GAP_WEIGHT * SCALE_FACTOR;

            cost += weightedGapCost;
        }

        return cost;
    }

    private static int getHeuristicGapChange(int id, Occupancy occupancy,int newPeriod, int dayIndex) {
        int firstPeriod = occupancy.getFirstOccupiedPeriod(id, dayIndex);
        int lastPeriod = occupancy.getLastOccupiedPeriod(id, dayIndex);

        int newFirst = Math.min(firstPeriod, newPeriod);
        int newLast = Math.max(lastPeriod, newPeriod);

        int gapChange = ((newLast - newFirst) - (lastPeriod - firstPeriod)) - 1;

        return gapChange;
    }

    public static int getOldContribution(SubjectGroup group, int[][] remainingLessonTable, int[] studentGapCount, int index) {
        int cost = 0;

        for (int id : group.getStudents()) {
            int lessonsLeft = remainingLessonTable[id][index];
            cost += Math.max(0, lessonsLeft - studentGapCount[id]) * GAP_WEIGHT;
        }

        return cost;
    }

    public static int convertWeight(double weight) {
        return (int)Math.round(weight * SCALE_FACTOR);
    }
}
