package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

import java.util.*;

public class PathFinder implements Search {
    private final Heap openHeap;
    private final ChildGenerator childGenerator;
    private final List<SubjectGroup> lessonInstances;
    private final ScheduleState initialState;

    public PathFinder(TestData data, boolean heuristic, boolean depthBased, boolean advanced) {
        openHeap = new Heap(depthBased);
        childGenerator = new ChildGenerator(data, true, true, heuristic, advanced);
        initialState = new ScheduleState(null, null, 0,0,0,0);
        this.lessonInstances = childGenerator.getLessonInstances();
    }

    @Override
    public ScheduleState find() {
        childGenerator.resetStats();
        childGenerator.clearSet();
        openHeap.offer(initialState);
        int pollCount = 0;
        int maxDepth = 0;
        int maxHeapSize = 0;
        int lastSeenIndex = 0;

        while (!openHeap.isEmpty()) {
            ScheduleState state = openHeap.poll();

            pollCount++;

            if (pollCount % 1000000 == 0) {
                System.out.println("Poll Count: " + pollCount);
                System.out.println("Heap Size: " + openHeap.size());
                printDepthStats();
                //System.out.println(closedSet.size());
            }

            if (state.getIndex() == lessonInstances.size()) {
                System.out.println("pollCount: " + pollCount);
                System.out.println("maxDepth: " + maxDepth);
                System.out.println("maxHeapSize: " + maxHeapSize);
                //System.out.println("closedSet: " + closedSet.size());
                System.out.println("HEAP size: " + openHeap.size());
                System.out.println(lessonInstances.size());
                printDepthStats();

                return state;
            }

            int index = state.getIndex();

            if (index > lastSeenIndex) {
                lastSeenIndex = index;


                System.out.println(index);

                //System.out.println(state.getPlacedLesson().getGroup());
                //System.out.println(state.getPlacedLesson().getRoom().getType());
                System.out.println("cost: " + state.getTotalCost());
                System.out.println("cannotPlaceCount: " + childGenerator.cannotPlaceCount);
                System.out.println("HEAP size: " + openHeap.size());
                //System.out.println("closedSet: " + closedSet.size());
            }

            maxHeapSize = Math.max(maxHeapSize, openHeap.size());
            maxDepth = Math.max(maxDepth, index);

            List<ScheduleState> childStates = childGenerator.generateChildren(state);

            for (ScheduleState child : childStates) {
                openHeap.offer(child);
            }
        }

        throw new RuntimeException("NO SCHEDULE FOUND");
    }

    private void printDepthStats() {
        int[] dead = childGenerator.deadEndsPerDepth;
        int[] dupe = childGenerator.dupeEmptiedPerDepth;

        for (int i = 0; i < dead.length; i++) {
            if (dead[i] > 0 || dupe[i] > 0) {
                System.out.println("depth " + i + " dead ends: " + dead[i] + " emptied by dupes: " + dupe[i]);
            }
        }

        System.out.println("children generated: " + childGenerator.totalChildren
                           + " new: " + childGenerator.newChildren);
    }
}
