package berlage.pws.model;

import java.util.Objects;
import java.util.Set;

public class Teacher {
    private final int[] available;
    private final int id;
    private final int countWorkingDays;
    private final Set<Subject> subjects;
    private final RoomType typeRequirement;

    public Teacher(int id, int[] available, Set<Subject> subjects, RoomType typeRequirement) {
        this.id = id;
        this.available = available;
        this.subjects = subjects;
        this.typeRequirement = typeRequirement;
        countWorkingDays = calculateCountWorkingDays();
    }

    public int getId() {
        return id;
    }

    public boolean isAvailable(int day, int slot) {
        int bits = available[day];
        return (bits & (1 << slot)) != 0;
    }
    
    public Set<Subject> getSubjects() {
        return subjects;
    }

    public int[] getAvailable() {
        return available;
    }

    public int getAvailableBitsOnDay(int dayIndex) {
        return available[dayIndex];
    }

    public int getCountWorkingDays() {
        return countWorkingDays;
    }

    private int calculateCountWorkingDays() {
        int count = 0;

        for (int i = 0; i < available.length; i++) {
            if (available[i] > 0) {
                count++;
            }
        }

        return count;
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
