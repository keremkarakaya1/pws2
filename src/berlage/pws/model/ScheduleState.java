package berlage.pws.model;

import java.util.Objects;

public class ScheduleState {
    private final ScheduleState parent;
    private final ScheduledLesson placedLesson;
    private final int index;
    private final int hCost;
    private final int gCost;

    public ScheduleState(ScheduleState parent, ScheduledLesson placedLesson, int hCost, int gCost, int index) {
        this.parent = parent;
        this.placedLesson = placedLesson;
        this.hCost = hCost;
        this.gCost = gCost;
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleState that = (ScheduleState) o;
        return hCost == that.hCost && gCost == that.gCost && Objects.equals(parent, that.parent) &&
               Objects.equals(placedLesson, that.placedLesson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, placedLesson, hCost, gCost);
    }

    public int getIndex() {
        return index;
    }
}
