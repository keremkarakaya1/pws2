package berlage.pws.model;

public class ScheduleState {
    private final ScheduleState parent;
    private final ScheduledLesson placedLesson;
    private final int index;
    private final int hCost;
    private final int gCost;

    public ScheduleState(ScheduleState parent, ScheduledLesson placedLesson, int hCost, int gCost, int index) { //, long delta
        this.parent = parent;
        this.placedLesson = placedLesson;
        this.hCost = hCost;
        this.gCost = gCost;
        this.index = index;
    }

    public ScheduleState getParent() {
        return parent;
    }

    public int getGCost() {
        return gCost;
    }

    public int getHCost() {
        return hCost;
    }

    public ScheduledLesson getPlacedLesson() {
        return placedLesson;
    }

    public int getIndex() {
        return index;
    }

    public int getTotalCost() {
        return hCost + gCost;
    }
}