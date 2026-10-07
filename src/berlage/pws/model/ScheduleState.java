package berlage.pws.model;

public class ScheduleState {
    private final ScheduleState parent;
    private final ScheduledLesson placedLesson;
    private final int index;
    private final int hCost;
    private final int gCost;
    private final long hash;

    public ScheduleState(ScheduleState parent, ScheduledLesson placedLesson, int hCost, int gCost, int index, long delta) {
        this.parent = parent;
        this.placedLesson = placedLesson;
        this.hCost = hCost;
        this.gCost = gCost;
        this.index = index;
        hash = (parent == null ? 0 : parent.hash) ^ delta;
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

    public long getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleState that = (ScheduleState) o;
        return hash == that.hash;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hash);
    }

}
