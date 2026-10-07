package berlage.pws.model;

import java.util.Objects;

public class Slot {
    private final int day;
    private final int period;
    private final int flatIndex;

    public Slot(int day, int period, int flatIndex) {
        this.day = day;
        this.period = period;
        this.flatIndex = flatIndex;
    }

    public int getFlatIndex() {
        return flatIndex;
    }

    public int getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return day == slot.day && period == slot.period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, period);
    }
}
