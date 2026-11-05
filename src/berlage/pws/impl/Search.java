package berlage.pws.impl;

import berlage.pws.model.ScheduleState;

public interface Search {
    ScheduleState find();

    default ScheduleState findOrDefault(ScheduleState defaultValue) {
        try {
            ScheduleState result = find();
            return result != null ? result : defaultValue;
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }
}
