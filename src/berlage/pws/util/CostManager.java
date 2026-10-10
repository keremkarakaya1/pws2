package berlage.pws.util;

import berlage.pws.model.ScheduledLesson;
import berlage.pws.model.Slot;
import berlage.pws.model.Student;
import berlage.pws.model.SubjectGroup;

public final class CostManager {
    private static final int SPAN_WEIGHT = 3;
    private static final int SPREAD_WEIGHT = 1;

    private CostManager() {}

    public static int calculateCost(Occupancy occupancy, ScheduledLesson lesson) {
        SubjectGroup group = lesson.getGroup();
        Slot slot = lesson.getSlot();
        int newPeriod = slot.getPeriod();
        int dayIndex = slot.getDay();
        int cost = 0;

        for (Student student : group.getStudents()) {
            int lessonCount = occupancy.getLessonCountOfStudentOnDay(student, dayIndex);

            if (lessonCount == 0) {
                cost += SPAN_WEIGHT + SPREAD_WEIGHT;
                continue;
            }

            int firstPeriod = occupancy.getFirstOccupiedPeriod(student, dayIndex);
            int lastPeriod = occupancy.getLastOccupiedPeriod(student, dayIndex);

            int newFirst = Math.min(firstPeriod, newPeriod);
            int newLast = Math.max(lastPeriod, newPeriod);

            int spanCostDelta = ((newLast - newFirst) - (lastPeriod - firstPeriod));
            int weightedSpanDelta = spanCostDelta * SPAN_WEIGHT;

            int spreadCostDelta = 2 * lessonCount + 1;
            int weightedSpreadCostDelta = spreadCostDelta * SPREAD_WEIGHT;

            cost += weightedSpanDelta + weightedSpreadCostDelta;
        }

        return cost;
    }
}
