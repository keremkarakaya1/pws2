package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.CostManager;
import berlage.pws.util.Occupancy;
import berlage.pws.util.TestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChildGenerator {
    private final Slot[] slots;
    private final Occupancy occupancy;

    private final ZobristHashing zobristHashing;
    private final List<SubjectGroup> lessonInstances;
    private final Map<RoomType, List<Room>> roomMap;

    private int cannotPlaceCount = 0;

    public ChildGenerator(List<SubjectGroup> lessonsInstances, TestData data) {
        int groupCount = 0;
        for (Set<SubjectGroup> set : data.subjectGroups().values()) {
            groupCount += set.size();
        }

        occupancy = new Occupancy(
                data.teacherCount(),
                data.roomCount(),
                data.students().size(),
                data.lessonsPerDay(),
                data.days(),
                data.maxSameSubjectCountPerDay(),
                groupCount);

        this.lessonInstances = lessonsInstances;
        this.slots = data.slots();
        roomMap = data.roomMap();
        zobristHashing = data.zobristHashing();
    }

    public List<ScheduleState> generateChildren(ScheduleState state) {
        List<ScheduleState> stateList = new ArrayList<>();
        occupancy.buildFullState(state);

        for (Slot slot : slots) {
            //List<Room> matchingRooms = roomMap.get(group.getTeacher().getTypeRequirement());
            //currently picking first available room
            int index = state.getIndex();
            SubjectGroup group = lessonInstances.get(index);
            RoomType requiredType = group.getTeacher().getTypeRequirement();
            Room room = getFreeRoom(requiredType, slot);

            if (room == null) {
                continue;
            }

            if (!occupancy.canPlace(group, slot)) {
                cannotPlaceCount++;
                continue;
            }

            ScheduledLesson scheduledLesson = new ScheduledLesson(group, slot, room);
            long delta = zobristHashing.hashFor(scheduledLesson);
            int hCost = 0;
            int gCost = state.getGCost() + CostManager.calculateCost(occupancy, scheduledLesson);


            ScheduleState childState = new ScheduleState(
                    state,
                    scheduledLesson,
                    hCost,
                    gCost,
                    index + 1,
                    delta);

            stateList.add(childState);
        }

        return stateList;
    }

    public Occupancy getOccupancy() {
        return occupancy;
    }

    public Slot[] getSlots() {
        return slots;
    }

    public List<SubjectGroup> getLessonInstances() {
        return lessonInstances;
    }

    private Room getFreeRoom(RoomType type, Slot slot) {
        List<Room> rooms = roomMap.get(type);

        for (Room room : rooms) {
            if (!occupancy.isRoomBusy(room, slot)) {
                return room;
            }
        }

        return null;
    }
}
