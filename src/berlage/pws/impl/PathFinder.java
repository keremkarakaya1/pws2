package berlage.pws.impl;

import berlage.pws.model.*;
import berlage.pws.util.BoundTracker;
import berlage.pws.util.CostManager;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

import java.util.*;

public class PathFinder implements Search {
    private final BoundTracker tracker;
    private final Heap openHeap;
    private final ChildGenerator childGenerator;
    private final List<SubjectGroup> lessonInstances;
    private final ScheduleState initialState;
    private final ScheduleState beamSeed;
    private final int weight;
    private int beamBest = Integer.MAX_VALUE;
    private int bound;
    private final int theoreticalBest;
    private int lowerBound;

    public PathFinder(TestData data, boolean heuristic, boolean advanced, boolean depthBased) {
        this(data, heuristic, advanced, depthBased, 1, null);
    }

    public PathFinder(TestData data, boolean heuristic, boolean advanced, boolean depthBased, double weight) {
        this(data, heuristic, advanced ,depthBased, weight, null);
    }

    public PathFinder(TestData data, boolean heuristic, boolean advanced, boolean depthBased,
                      double weight, Search algorithm) {
        if (weight < 1) {
            throw new IllegalArgumentException("PathFinder weight may not be less than 1");
        }

        this.weight = CostManager.convertWeight(weight);

        openHeap = new Heap(depthBased);
        childGenerator = new ChildGenerator(data, heuristic, advanced, this.weight);
        initialState = new ScheduleState(null, null, 0, 0, 0);
        tracker = new BoundTracker(data.lessonInstances(), this.weight);

        theoreticalBest = CostManager.getTheoreticalBest(data.lessonInstances());
        this.lessonInstances = childGenerator.getLessonInstances();


        if (algorithm != null) {
            beamSeed = algorithm.findOrDefault(null);

            if (beamSeed != null) {
                beamBest = beamSeed.getTotalCost();
            }
        } else {
            beamSeed = null;
        }
    }

    @Override
    public ScheduleState find() {
        childGenerator.reset();
        openHeap.clear();
        tracker.reset();
        openHeap.offer(initialState);
        tracker.track(initialState, true);
        int pollCount = 0;
        int maxDepth = 0;
        long maxHeapSize = 0;
        int lastSeenIndex = 0;

        while (!openHeap.isEmpty()) {
            ScheduleState state = openHeap.poll();
            tracker.track(state, false);

            pollCount++;

            if (state.getIndex() == lessonInstances.size()) {
                printGoalStatistics(state, maxHeapSize, pollCount, maxDepth);
                return state;
            }

            int index = state.getIndex();

            if (index > lastSeenIndex) {
                lastSeenIndex = index;


                System.out.println(index);

                //System.out.println(state.getPlacedLesson().getGroup());
                //System.out.println(state.getPlacedLesson().getRoom().getType());
                System.out.println("cost: " + state.getTotalCost() / (double)CostManager.SCALE_FACTOR);
                System.out.println("cannotPlaceCount: " + childGenerator.cannotPlaceCount);
                System.out.println("HEAP size: " + openHeap.size());
                //System.out.println("closedSet: " + closedSet.size());
            }

            maxHeapSize = Math.max(maxHeapSize, openHeap.size());
            maxDepth = Math.max(maxDepth, index);

            List<ScheduleState> childStates = childGenerator.generateChildren(state);

            for (int i = 0; i < childStates.size(); i++) {
                ScheduleState childState = childStates.get(i);

                if (childState.getGCost() + childState.getHCost() / weight * CostManager.SCALE_FACTOR >= beamBest) {
                    continue;
                }

                tracker.track(childState, true);
                openHeap.offer(childStates.get(i));
            }

            if (pollCount % 1000000 == 0) {
                System.out.println("Poll Count: " + pollCount);
                System.out.println("Heap Size: " + openHeap.size());
                bound = tracker.getBound();
                System.out.println("BOUND: " + bound / (double) CostManager.SCALE_FACTOR);
                //System.out.println(closedSet.size());
            }
        }

        if (beamBest == Integer.MAX_VALUE) {
            throw new RuntimeException("NO SCHEDULE FOUND");
        }

        printGoalStatistics(beamSeed, maxHeapSize, pollCount, maxDepth);
        System.out.println("OPTIMAL SCHEDULE FOUND BY SEED: " + beamSeed.getTotalCost() / (double) CostManager.SCALE_FACTOR);
        return beamSeed;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    private void printGoalStatistics(ScheduleState state, long maxHeapSize, int pollCount, int maxDepth) {
        System.out.println("pollCount: " + pollCount);
        System.out.println("maxDepth: " + maxDepth);
        System.out.println("maxHeapSize: " + maxHeapSize);
        //System.out.println("closedSet: " + closedSet.size());
        System.out.println("HEAP size: " + openHeap.size());

        int found = state.getTotalCost();
        bound = tracker.getBound();
        int weightedFloor = (int) ((long) found * CostManager.SCALE_FACTOR / weight);
        lowerBound = Math.min(found, Math.max(bound, Math.max(theoreticalBest, weightedFloor)));
        System.out.println("BOUND: " + lowerBound / (double) CostManager.SCALE_FACTOR + " (found cost " + found / (double)CostManager.SCALE_FACTOR + ")");
        System.out.println(lessonInstances.size());
    }
}
