package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.CostManager;
import berlage.pws.util.GapCost;
import berlage.pws.util.Occupancy;
import berlage.pws.util.TestData;

import java.util.*;

public class ChildGenerator {
    private final Occupancy occupancy;
    private final GapCost gapCost;
    private final List<SubjectGroup> lessonInstances;
    private final Map<RoomType, Room[]> roomMap;
    private final Map<Teacher, int[]> studentsOfTeacher = new HashMap<>();
    private final List<ScheduleState> stateList = new ArrayList<>();
    private final ScheduledLesson[][][] placements;
    private final Slot[] slots;
    private final boolean heuristic;
    private final int weight;

    public ChildGenerator(TestData data, boolean heuristic, int weight) {
        this.heuristic = heuristic;
        this.weight = weight;
        slots = data.slots();
        roomMap = data.roomMap();
        lessonInstances = data.lessonInstances();

        occupancy = new Occupancy(
                data.teacherCount(),
                data.roomCount(),
                data.students().size(),
                data.lessonsPerDay(),
                data.days(),
                data.maxSameSubjectCountPerDay(),
                data.groupCount());

        gapCost = new GapCost(occupancy,
                data.subjectGroupsPerStudent(),
                data.countGroupsToBeScheduledAtIndex(),
                data.days(),
                data.students().size());

        // A student must appear once per teacher, or the cost deltas would double count.
        data.studentsOfTeacherMap().forEach((teacher, ids) ->
                studentsOfTeacher.put(teacher, Arrays.stream(ids).distinct().toArray()));

        placements = new ScheduledLesson[data.groupCount()][slots.length][data.roomCount()];

        for (Set<SubjectGroup> groups : data.subjectGroups().values()) {
            for (SubjectGroup group : groups) {
                for (Room room : roomMap.get(group.getTeacher().getTypeRequirement())) {
                    for (Slot slot : slots) {
                        placements[group.getId()][slot.getFlatIndex()][room.getId()] =
                                new ScheduledLesson(group, slot, room);
                    }
                }
            }
        }
    }

    public List<ScheduleState> generateChildren(ScheduleState state) {
        int index = state.getIndex();

        if (index == lessonInstances.size()) {
            throw new IllegalStateException("COMPLETE SCHEDULE CHILDREN REQUEST");
        }

        stateList.clear();
        occupancy.buildFullState(state);
        gapCost.startParent(index);

        SubjectGroup group = lessonInstances.get(index);
        Teacher teacher = group.getTeacher();
        int[] teacherStudents = studentsOfTeacher.get(teacher);
        ScheduledLesson[][] options = placements[group.getId()];

        // Identical consecutive lessons are placed in increasing slot order (symmetry breaking).
        int first = 0;

        if (index > 0 && lessonInstances.get(index - 1) == group) {
            first = state.getPlacedLesson().getSlot().getFlatIndex() + 1;
        }

        for (int s = first; s < slots.length; s++) {
            Slot slot = slots[s];

            if (!occupancy.canPlace(group, slot)) {
                continue;
            }

            Room room = getFreeRoom(teacher.getTypeRequirement(), slot);

            if (room == null) {
                continue;
            }

            ScheduledLesson lesson = options[s][room.getId()];
            gapCost.evaluate(lesson, teacherStudents);

            int gCost = state.getGCost() + gapCost.deadDelta() * CostManager.SCALE_FACTOR;
            int hCost = heuristic ? state.getHCost() + gapCost.boundDelta() * weight : 0;

            stateList.add(new ScheduleState(state, lesson, hCost, gCost, index + 1));
        }

        return stateList;
    }

    public void reset() {
        occupancy.reset();
    }

    public List<SubjectGroup> getLessonInstances() {
        return lessonInstances;
    }

    private Room getFreeRoom(RoomType type, Slot slot) {
        for (Room room : roomMap.get(type)) {
            if (!occupancy.isRoomBusy(room, slot)) {
                return room;
            }
        }

        return null;
    }
}