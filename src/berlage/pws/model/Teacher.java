package berlage.pws.model;

import java.util.Objects;
import java.util.Set;

public class Teacher {
    private final boolean[][] available;
    private final boolean[] defaultAvailable;
    private final int id;
    private final int countAvailableDays;
    private final Set<Subject> subjects;
    //private final Set<Room> requiredRoomSet;
    private final RoomType typeRequirement;

    public Teacher(int id, int countAvailableDays, boolean[] defaultAvailable, boolean[][] available, Set<Subject> subjects, RoomType typeRequirement) {
        this.id = id;
        this.available = available;
        this.defaultAvailable = defaultAvailable;
        this.subjects = subjects;
        //this.requiredRoomSet = roomSet;
        this.typeRequirement = typeRequirement;
        this.countAvailableDays = countAvailableDays;
    }

    public int getId() {
        return id;
    }

    public boolean isAvailable(int day, int slot) {
        return available[day][slot];
    }
    
    public Set<Subject> getSubjects() {
        return subjects;
    }

    /*
    public Set<Room> getRequiredRoomSet() {
        return requiredRoomSet;
    }
     */

    public boolean[] getDefaultAvailable() {
        return defaultAvailable;
    }

    public RoomType getTypeRequirement() {
        return typeRequirement;
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
