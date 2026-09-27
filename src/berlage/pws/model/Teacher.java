package berlage.pws.model;

import java.util.Set;

public class Teacher {
    private final boolean[][] available;
    private final int id;
    private final Subject subject;
    private final Set<Room> requiredRoomSet;

    public Teacher(int id, boolean[][] available, Subject subject, Set<Room> roomSet) {
        this.id = id;
        this.available = available;
        this.subject = subject;
        this.requiredRoomSet = roomSet;
    }

    public int getId() {
        return id;
    }

    public boolean isAvailable(int day, int slot) {
        return available[day][slot];
    }

    public void markAvailability(int day, int slot, boolean state) {
        available[day][slot] = state;
    }

    public Subject getSubject() {
        return subject;
    }

    public Set<Room> getRequiredRoomSet() {
        return requiredRoomSet;
    }
}
