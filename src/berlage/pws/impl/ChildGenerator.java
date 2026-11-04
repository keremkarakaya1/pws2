package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.CostManager;
import berlage.pws.util.Occupancy;
import berlage.pws.util.TestData;

import java.util.*;

public class ChildGenerator {
    private final Slot[] slots;
    private final int[][] lessonsLeft;
    private final int[][] countGroupsToBeScheduled;
    private final Occupancy occupancy;
    private final List<SubjectGroup> lessonInstances;
    private final List<List<SubjectGroup>> subjectGroupsPerStudent;
    private final Map<RoomType, List<Room>> roomMap;
    private final List<ScheduleState> stateList;
    private final int[] perStudentGapCount;
    private final int[] students;
    private final int days;
    private final boolean gapPenalty;
    private final boolean gapOnly;
    private final boolean heuristic;
    private final boolean advanced;
    private final int weight;
    int cannotPlaceCount = 0;

    public ChildGenerator(TestData data, boolean gapPenalty, boolean gapOnly, boolean heuristic, boolean advanced, int weight) {
        days = data.days();
        this.lessonInstances = data.lessonInstances();
        this.gapPenalty = gapPenalty;
        this.gapOnly = gapOnly;
        this.heuristic = heuristic;
        this.advanced = advanced;
        this.weight = weight;
        lessonsLeft = data.lessonsLeft();

        occupancy = new Occupancy(
                data.teacherCount(),
                data.roomCount(),
                data.students().size(),
                data.lessonsPerDay(),
                data.days(),
                data.maxSameSubjectCountPerDay(),
                data.groupCount());

        this.slots = data.slots();

        subjectGroupsPerStudent = data.subjectGroupsPerStudent();
        countGroupsToBeScheduled = data.countGroupsToBeScheduledAtIndex();
        students = new int[data.students().size()];

        int index = 0;
        for (Student student : data.students()) {
            students[index++] = student.getId();
        }

        perStudentGapCount = new int[students.length];
        stateList = new ArrayList<>();

        roomMap = data.roomMap();
    }

    public List<ScheduleState> generateChildren(ScheduleState state) {
        if (state.getIndex() == lessonInstances.size()) {
            throw new IllegalStateException("COMPLETE SCHEDULE CHILDREN REQUEST");
        }

        stateList.clear();
        occupancy.buildFullState(state);
        fillPerStudentGapCount();

        int index = state.getIndex();
        int parentHCost = 0;
        int oldGroupContribution = 0;

        if (heuristic && !advanced) {
            SubjectGroup groupToPlace = lessonInstances.get(index);

            parentHCost = state.getPlacedLesson() == null
                    ? CostManager.calculateFullHCost(students, lessonsLeft, perStudentGapCount, index) * weight
                    : state.getHCost();

            oldGroupContribution = CostManager.getOldContribution(groupToPlace, lessonsLeft, perStudentGapCount, index);
        }

        SubjectGroup previous = index == 0 ? null : lessonInstances.get(index - 1);
        int allowedFromIndex = -1;

        if (previous == lessonInstances.get(index)) {
            ScheduledLesson lesson = state.getPlacedLesson();
            Slot slot = lesson.getSlot();
            allowedFromIndex = slot.getFlatIndex();
        }

        for (Slot slot : slots) {
            //List<Room> matchingRooms = roomMap.get(group.getTeacher().getTypeRequirement());
            //currently picking first available room
            if (slot.getFlatIndex() <= allowedFromIndex) {
                continue;
            }

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

            int hCost = 0;
            int gCost = state.getGCost() + CostManager.calculateGCost(occupancy, scheduledLesson, gapPenalty, gapOnly);

            if (heuristic) {
                if (advanced) {
                    hCost = CostManager.calculateAdvancedHCost(occupancy,
                                                            scheduledLesson,
                                                            students,
                                                            subjectGroupsPerStudent,
                                                            countGroupsToBeScheduled,
                                                            lessonsLeft,
                                                            perStudentGapCount,
                                                            index + 1,
                                                            days,
                                                            weight);
                } else {
                    hCost = CostManager.calculateHCost(occupancy,
                                                    scheduledLesson,
                                                    lessonsLeft,
                                                    perStudentGapCount,
                                                    index,
                                                    parentHCost,
                                                    oldGroupContribution,
                                                    weight);
                }
            }


            ScheduleState childState = new ScheduleState(
                    state,
                    scheduledLesson,
                    hCost,
                    gCost,
                    index + 1);
                    //delta

            stateList.add(childState);
        }

        return stateList;
    }

    private void fillPerStudentGapCount() {
        for (int id : students) {
            int gapCount = 0;

            for (int i = 0; i < days; i++) {
                int lessonCount = occupancy.getLessonCountOfStudentOnDay(id, i);

                if (lessonCount == 0) {
                    continue;
                }

                int firstPeriod = occupancy.getFirstOccupiedPeriod(id, i);
                int lastPeriod = occupancy.getLastOccupiedPeriod(id, i);

                gapCount += lastPeriod - firstPeriod + 1 - lessonCount;
            }

            perStudentGapCount[id] = gapCount;
        }
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
