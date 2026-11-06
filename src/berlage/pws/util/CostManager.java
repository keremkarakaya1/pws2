package berlage.pws.util;

import berlage.pws.model.*;

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


    public static int calculateAdvancedHCost(Occupancy occupancy,
                                             ScheduledLesson lesson,
                                             int[] students,
                                             List<List<SubjectGroup>> subjectGroupsPerStudent,
                                             int[][] countGroupsToBeScheduledAtIndex,
                                             int[][] remainingLessonTable,
                                             int[] studentGapCount,
                                             int index,
                                             int days,
                                             int weight) {



        int[] updatedStudentGapCounts = studentGapCount.clone();
        SubjectGroup group = lesson.getGroup();
        Slot slot = lesson.getSlot();
        int dayIndex = slot.getDay();
        int newPeriod = slot.getPeriod();
        int cost = 0;

        for (int id : group.getStudents()) {
            int gapChange = 0;

            if (occupancy.getLessonCountOfStudentOnDay(id, dayIndex) != 0) {
                gapChange = getHeuristicGapChange(id, occupancy, newPeriod, dayIndex);
                updatedStudentGapCounts[id] += gapChange;
            }
        }

        occupancy.updateOccupancy(lesson, true);

        for (int id : students) {
            int gapCountBefore = updatedStudentGapCounts[id];
            int lessonsLeft = remainingLessonTable[id][index];

            for (int i = 0; i < days; i++) {
                if (occupancy.getLessonCountOfStudentOnDay(id,i) == 0) {
                    continue;
                }

                int j = 0;
                boolean[] lessons = occupancy.getStudentScheduleOnDay(id, i);

                while (j < lessons.length && !lessons[j]) {
                    j++;
                }

                for ( ; j < lessons.length; j++) {
                    if (lessons[j]) {
                        continue;
                    }

                    List<SubjectGroup> groupsOfStudent = subjectGroupsPerStudent.get(id);
                    boolean found = false;

                    for (SubjectGroup subjectGroup : groupsOfStudent) {
                        Teacher teacher = subjectGroup.getTeacher();

                        if (countGroupsToBeScheduledAtIndex[subjectGroup.getId()][index] > 0 &&
                            !occupancy.isTeacherBusy(teacher, i, j) &&
                            teacher.isAvailable(i,j)) {

                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        gapCountBefore--;
                    }
                }
            }

            int forcedGapCount = Math.max(0, lessonsLeft - gapCountBefore);
            cost += forcedGapCount * GAP_WEIGHT;
        }

        occupancy.updateOccupancy(lesson, false);
        return cost * weight;
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

    private static int getHeuristicGapChange(int id, Occupancy occupancy,int newPeriod, int dayIndex) {
        int firstPeriod = occupancy.getFirstOccupiedPeriod(id, dayIndex);
        int lastPeriod = occupancy.getLastOccupiedPeriod(id, dayIndex);

        int newFirst = Math.min(firstPeriod, newPeriod);
        int newLast = Math.max(lastPeriod, newPeriod);

        int gapChange = ((newLast - newFirst) - (lastPeriod - firstPeriod)) - 1;

        return gapChange;
    }

    public static int convertBeamCost(ScheduledLesson[][][] scheduledLessons) {
        int cost = 0;

        for (int i = 0; i < scheduledLessons.length; i++) {
            for (int j = 0; j < scheduledLessons[i].length; j++) {

                int start = 0;
                int end = scheduledLessons[i][j].length - 1;

                while (start <= end && scheduledLessons[i][j][start] == null) {
                    start++;
                }

                while (end >= start && scheduledLessons[i][j][end] == null) {
                    end--;
                }

                cost += (end - start + 1) * GAP_WEIGHT;
            }
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
