package berlage.pws.model;

public class ScheduledLesson {
    private final SubjectGroup group;
    private final Slot slot;
    private final Room room;

    public ScheduledLesson(SubjectGroup group, Slot slot, Room room) {
        this.group = group;
        this.slot = slot;
        this.room = room;
    }


    public SubjectGroup getGroup() {
        return group;
    }

    public Slot getSlot() {
        return slot;
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return group +  " "  + room;
    }
}
