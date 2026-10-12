package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

import java.util.*;

public class PathFinder implements Search {
    private final Heap openHeap;
    private final Set<Long> closedSet;
    private final ChildGenerator childGenerator;
    private final List<SubjectGroup> lessonInstances;
    private final ScheduleState initialState;

    public PathFinder(List<SubjectGroup> lessonsInstances, TestData data, boolean heuristic, boolean depthBased) {
        openHeap = new Heap(depthBased);
        closedSet = new HashSet<>();
        childGenerator = new ChildGenerator(lessonsInstances, data);

        initialState = new ScheduleState(null, null, 0,0,0,0);
        this.lessonInstances = childGenerator.getLessonInstances();
    }

    @Override
    public ScheduleState find() {
        openHeap.offer(initialState);
        int pollCount = 0;
        int maxDepth = 0;
        int maxHeapSize = 0;
        int lastSeenIndex = 0;
        int cannotPlaceCount = 0;

        while (!openHeap.isEmpty()) {
            ScheduleState state = openHeap.poll();
            closedSet.add(state.getHash());
            pollCount++;

            if (state.getIndex() == lessonInstances.size()) {
                System.out.println("pollCount: " + pollCount);
                System.out.println("maxDepth: " + maxDepth);
                System.out.println("maxHeapSize: " + maxHeapSize);
                System.out.println("closedSet: " + closedSet.size());
                System.out.println("HEAP size: " + openHeap.size());
                System.out.println(lessonInstances.size());

                for (int i = 0; i < 5; i++) {
                    System.out.println();
                }

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

            List<ScheduleState> childStates = childGenerator.generateChildren(state);

            for (ScheduleState child : childStates) {
                openHeap.offer(child);
            }
        }

        return null;
    }
}
