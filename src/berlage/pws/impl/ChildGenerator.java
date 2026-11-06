package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.CostManager;
import berlage.pws.util.Occupancy;
import berlage.pws.util.TestData;

import java.util.*;

public class ChildGenerator {
    private final Occupancy occupancy;
    private final List<SubjectGroup> lessonInstances;
    private final List<List<SubjectGroup>> subjectGroupsPerStudent;
    private final Map<RoomType, List<Room>> roomMap;
    private final List<ScheduleState> stateList;
    private final int[][][] placementIds;
    private final int[][] lessonsLeft;
    private final int[][] countGroupsToBeScheduled;
    private final int[] perStudentGapCount;
    private final int[] students;
    private final Slot[] slots;
    private ScheduledLesson[] placements;
    private final int days;
    private final boolean heuristic;
    private final boolean advanced;
    private final int weight;
    int cannotPlaceCount = 0;

    public ChildGenerator(TestData data, boolean heuristic, boolean advanced, int weight) {
        days = data.days();
        roomMap = data.roomMap();
        this.lessonInstances = data.lessonInstances();
        this.heuristic = heuristic;
        this.advanced = advanced;
        this.weight = weight;
        this.slots = data.slots();
        lessonsLeft = data.lessonsLeft();

        occupancy = new Occupancy(
                data.teacherCount(),
                data.roomCount(),
                data.students().size(),
                data.lessonsPerDay(),
                data.days(),
                data.maxSameSubjectCountPerDay(),
                data.groupCount());


        subjectGroupsPerStudent = data.subjectGroupsPerStudent();
        countGroupsToBeScheduled = data.countGroupsToBeScheduledAtIndex();
        students = new int[data.students().size()];
        perStudentGapCount = new int[students.length];
        stateList = new ArrayList<>();

        int placementId = 0;
        int upperBoundTotalPlacements = data.groupCount() * data.slots().length * data.roomCount();
        placements = new ScheduledLesson[upperBoundTotalPlacements];
        placementIds = new int[data.groupCount()][data.slots().length][data.roomCount()];

        for (Slot slot : slots) {
            for (Set<SubjectGroup> groups : data.subjectGroups().values()) {
                for (SubjectGroup group : groups) {
                    for (Room room : roomMap.get(group.getTeacher().getTypeRequirement())) {
                        placements[placementId] = new ScheduledLesson(group, slot, room);
                        placementIds[group.getId()][slot.getFlatIndex()][room.getId()] = placementId;
                        placementId++;
                    }
                }
            }
        }

        placements = Arrays.copyOf(placements, placementId);

        int index = 0;
        for (Student student : data.students()) {
            students[index++] = student.getId();
        }
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

            int placementId = placementIds[group.getId()][slot.getFlatIndex()][room.getId()];
            ScheduledLesson scheduledLesson = placements[placementId];

            int hCost = 0;
            int gCost = state.getGCost() + CostManager.calculateGCost(occupancy, scheduledLesson);

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

    public void reset() {
        occupancy.reset();
    }

    public List<SubjectGroup> getLessonInstances() {
        return lessonInstances;
    }

    private Room getFreeRoom(RoomType type, Slot slot) {
        List<Room> rooms = roomMap.get(type);

        for (int i = 0; i < rooms.size(); i++) {
            if (!occupancy.isRoomBusy(rooms.get(i), slot)) {
                return rooms.get(i);
            }
        }

        return null;
    }
}
