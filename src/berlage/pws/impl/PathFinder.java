package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.CostManager;
import berlage.pws.util.Heap;
import berlage.pws.util.Occupancy;
import berlage.pws.util.TestData;

import java.util.*;

public class PathFinder {
    private final Heap openHeap;
    private final Set<Long> closedSet;
    private final ScheduleState initialState;
    private final Occupancy occupancy;
    private final ZobristHashing zobristHashing;
    private final List<SubjectGroup> lessonsInstances;
    private final Map<RoomType, List<Room>> roomMap;
    private final Slot[] slots;

    public PathFinder(boolean heuristic, TestData data, List<SubjectGroup> lessonsInstances) {
        this.lessonsInstances = lessonsInstances;

        int groupCount = 0;
        for (Set<SubjectGroup> set : data.subjectGroups().values()) {
            groupCount += set.size();
        }

        openHeap = new Heap();
        occupancy = new Occupancy(
                data.teacherCount(),
                data.roomCount(),
                data.students().size(),
                data.lessonsPerDay(),
                data.days(),
                data.maxSameSubjectCountPerDay(),
                groupCount);

        initialState = new ScheduleState(null, null, 0,0,0,0);
        closedSet = new HashSet<>();
        zobristHashing = data.zobristHashing();
        roomMap = data.roomMap();
        slots = data.slots();
    }

    public ScheduleState find() {
        openHeap.offer(initialState);
        int pollCount = 0;
        int maxDepth = 0;
        int maxHeapSize = 0;
        int lastSeenIndex = 0;
        int cannotPlaceCount = 0;

        while (!openHeap.isEmpty()) {
            ScheduleState state = openHeap.poll();
            pollCount++;
            occupancy.buildFullState(state);
            closedSet.add(state.getHash());

            if (state.getIndex() == lessonsInstances.size()) {
                System.out.println("pollCount: " + pollCount);
                System.out.println("maxDepth: " + maxDepth);
                System.out.println("maxHeapSize: " + maxHeapSize);
                System.out.println("closedSet: " + closedSet.size());
                System.out.println("HEAP size: " + openHeap.size());
                System.out.println(lessonsInstances.size());
                return state;
            }

            int index = state.getIndex();

            if (index > lastSeenIndex) {
                lastSeenIndex = index;
                System.out.println(index);
                System.out.println(state.getPlacedLesson().getGroup());
                System.out.println(state.getPlacedLesson().getRoom().getType());
                System.out.println("cannotPlaceCount: " +cannotPlaceCount);
                System.out.println("HEAP size: " + openHeap.size());
                System.out.println("closedSet: " + closedSet.size());
            }

            maxHeapSize = Math.max(maxHeapSize, openHeap.size());
            maxDepth = Math.max(maxDepth, index);
            SubjectGroup group = lessonsInstances.get(index);
            RoomType requiredType = group.getTeacher().getTypeRequirement();

            for (Slot slot : slots) {
                //List<Room> matchingRooms = roomMap.get(group.getTeacher().getTypeRequirement());
                //currently picking first available room
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

                if (closedSet.contains(childState.getHash())) {
                    continue;
                }

                openHeap.offer(childState);
            }


        }

        return null;
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
