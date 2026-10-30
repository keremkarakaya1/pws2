package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.ArraySet;
import berlage.pws.util.CostManager;
import berlage.pws.util.Occupancy;
import berlage.pws.util.TestData;

import java.util.*;

public class ChildGenerator {
    private final Slot[] slots;
    private final int[][] lessonsLeft;
    private final int[][] countGroupsToBeScheduled;
    private final Occupancy occupancy;
    private final ZobristHashing zobristHashing;
    private final List<SubjectGroup> lessonInstances;
    private final List<List<SubjectGroup>> subjectGroupsPerStudent;
    private final Map<RoomType, List<Room>> roomMap;
    private final List<Student> studentList;
    private final ArraySet closedSet;
    private final int[] perStudentGapCount;
    private final int days;
    private final boolean gapPenalty;
    private final boolean gapOnly;
    private final boolean heuristic;
    private final boolean advanced;
    private final int initialPower = 28;
    private final int weight;
    int cannotPlaceCount = 0;
    int counter = 0;

    final int[] deadEndsPerDepth;
    final int[] dupeEmptiedPerDepth;
    long totalChildren = 0;
    long newChildren = 0;

    public ChildGenerator(TestData data, boolean gapPenalty, boolean gapOnly, boolean heuristic, boolean advanced, int weight) {
        days = data.days();
        this.lessonInstances = data.lessonInstances();
        this.gapPenalty = gapPenalty;
        this.gapOnly = gapOnly;
        this.heuristic = heuristic;
        this.advanced = advanced;
        this.weight = weight;
        lessonsLeft = data.lessonsLeft();

        deadEndsPerDepth = new int[lessonInstances.size() + 1];
        dupeEmptiedPerDepth = new int[lessonInstances.size() + 1];

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
        studentList = new ArrayList<>(data.students());
        closedSet = new ArraySet(initialPower);
        perStudentGapCount = new int[studentList.size()];

        roomMap = data.roomMap();
        zobristHashing = data.zobristHashing();
    }

    public List<ScheduleState> generateChildren(ScheduleState state) {
        if (state.getIndex() == lessonInstances.size()) {
            throw new IllegalStateException("COMPLETE SCHEDULE CHILDREN REQUEST");
        }

        List<ScheduleState> stateList = new ArrayList<>();
        occupancy.buildFullState(state);
        fillPerStudentGapCount();
        int index = state.getIndex();
        boolean legalFound = false;

        for (Slot slot : slots) {
            //List<Room> matchingRooms = roomMap.get(group.getTeacher().getTypeRequirement());
            //currently picking first available room
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

            legalFound = true;

            ScheduledLesson scheduledLesson = new ScheduledLesson(group, slot, room);
            long delta = zobristHashing.hashFor(scheduledLesson);
            long newHash = state.getHash() ^ delta;

            totalChildren++;

            if (!closedSet.add(newHash)) {
                counter++;

                if (counter % 100000 == 0) {
                    System.out.println("Dupecount: " + counter);
                }

                continue;
            }

            newChildren++;

            int hCost = 0;
            int gCost = state.getGCost() + CostManager.calculateGCost(occupancy, scheduledLesson, gapPenalty, gapOnly);

            if (heuristic) {
                if (advanced) {
                    hCost = CostManager.calculateAdvancedHCost(occupancy,
                                                            scheduledLesson,
                                                            studentList,
                                                            subjectGroupsPerStudent,
                                                            countGroupsToBeScheduled,
                                                            lessonsLeft,
                                                            perStudentGapCount,
                                                            index + 1,
                                                            days);
                } else {
                    hCost = CostManager.calculateHCost(occupancy,
                                                    scheduledLesson,
                                                    studentList,
                                                    lessonsLeft,
                                                    perStudentGapCount,
                                                    index + 1,
                                                    weight);
                }
            }


            ScheduleState childState = new ScheduleState(
                    state,
                    scheduledLesson,
                    hCost,
                    gCost,
                    index + 1,
                    delta);

            stateList.add(childState);
        }


        if (stateList.isEmpty()) {
            if (legalFound) {
                dupeEmptiedPerDepth[index]++;
            } else {
                deadEndsPerDepth[index]++;
            }
        }

        return stateList;
    }

    private void fillPerStudentGapCount() {
        for (Student student : studentList) {
            int gapCount = 0;

            for (int i = 0; i < days; i++) {
                int lessonCount = occupancy.getLessonCountOfStudentOnDay(student, i);

                if (lessonCount == 0) {
                    continue;
                }

                int firstPeriod = occupancy.getFirstOccupiedPeriod(student, i);
                int lastPeriod = occupancy.getLastOccupiedPeriod(student, i);

                gapCount += lastPeriod - firstPeriod + 1 - lessonCount;
            }

            perStudentGapCount[student.getId()] = gapCount;
        }
    }

    public void resetStats() {
        Arrays.fill(deadEndsPerDepth, 0);
        Arrays.fill(dupeEmptiedPerDepth, 0);
        totalChildren = 0;
        newChildren = 0;
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

    public void clearSet() {
        closedSet.clear();
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
