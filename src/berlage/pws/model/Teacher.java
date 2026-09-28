package berlage.pws.model;

import java.util.Objects;
import java.util.Set;

public class Teacher {
    private final boolean[][] available;
    private final boolean[] defaultAvailable;
    private final int id;
    private final Set<Subject> subjects;
    private final Set<Room> requiredRoomSet;

    public Teacher(int id, boolean[] defaultAvailable, boolean[][] available, Set<Subject> subjects, Set<Room> roomSet) {
        this.id = id;
        this.available = available;
        this.defaultAvailable = defaultAvailable;
        this.subjects = subjects;
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

    public Set<Subject> getSubject() {
        return subjects;
    }

    public Set<Room> getRequiredRoomSet() {
        return requiredRoomSet;
    }

    public boolean[] getDefaultAvailable() {
        return defaultAvailable;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return id == teacher.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
