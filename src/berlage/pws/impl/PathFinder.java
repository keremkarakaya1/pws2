package berlage.pws.impl;

import berlage.pws.model.ScheduleState;
import berlage.pws.util.BoundTracker;
import berlage.pws.util.CostManager;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

public class PathFinder implements Search {
    private final BoundTracker tracker;
    private final Heap openHeap;
    private final ChildGenerator childGenerator;
    private final ScheduleState initialState = new ScheduleState(null, null, 0, 0, 0);
    private final ScheduleState seed;
    private final int seedCost;
    private final int lessonCount;
    private final int weight;
    private int lowerBound;

    public PathFinder(TestData data, boolean heuristic, boolean depthBased) {
        this(data, heuristic, depthBased, 1, null);
    }

    public PathFinder(TestData data, boolean heuristic, boolean depthBased, double weight) {
        this(data, heuristic, depthBased, weight, null);
    }

    public PathFinder(TestData data, boolean heuristic, boolean depthBased, double weight, Search seedSearch) {
        if (weight < 1) {
            throw new IllegalArgumentException("PathFinder weight may not be less than 1");
        }

        this.weight = CostManager.convertWeight(weight);
        openHeap = new Heap(depthBased);
        childGenerator = new ChildGenerator(data, heuristic, this.weight);
        tracker = new BoundTracker(this.weight);
        lessonCount = data.lessonInstances().size();

        seed = seedSearch == null ? null : seedSearch.findOrDefault(null);
        seedCost = seed == null ? Integer.MAX_VALUE : seed.getTotalCost();
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

        while (!openHeap.isEmpty()) {
            ScheduleState state = openHeap.poll();
            tracker.track(state, false);
            pollCount++;

            int index = state.getIndex();

            if (index == lessonCount) {
                printGoalStatistics(state, maxHeapSize, pollCount, maxDepth);
                return state;
            }

            if (index > maxDepth) {
                maxDepth = index;
                System.out.println(index + " f: " + state.getTotalCost() / (double) CostManager.SCALE_FACTOR
                                   + " heap: " + openHeap.size());
            }

            maxHeapSize = Math.max(maxHeapSize, openHeap.size());

            for (ScheduleState child : childGenerator.generateChildren(state)) {
                // Unweighted f, in cost units. Anything not better than the seed cannot improve on it.
                if (child.getGCost() + child.getHCost() / weight * CostManager.SCALE_FACTOR >= seedCost) {
                    continue;
                }

                tracker.track(child, true);
                openHeap.offer(child);
            }

            if (pollCount % 1_000_000 == 0) {
                System.out.println("Poll Count: " + pollCount + " Heap Size: " + openHeap.size()
                                   + " BOUND: " + tracker.getBound() / (double) CostManager.SCALE_FACTOR);
            }
        }

        if (seed == null) {
            throw new RuntimeException("NO SCHEDULE FOUND");
        }

        printGoalStatistics(seed, maxHeapSize, pollCount, maxDepth);
        System.out.println("OPTIMAL SCHEDULE FOUND BY SEED: " + seedCost / (double) CostManager.SCALE_FACTOR);
        return seed;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    private void printGoalStatistics(ScheduleState state, long maxHeapSize, int pollCount, int maxDepth) {
        int found = state.getTotalCost();
        int weightedFloor = (int) ((long) found * CostManager.SCALE_FACTOR / weight);
        lowerBound = Math.min(found, Math.max(tracker.getBound(), weightedFloor));

        System.out.println("pollCount: " + pollCount);
        System.out.println("maxDepth: " + maxDepth);
        System.out.println("maxHeapSize: " + maxHeapSize);
        System.out.println("HEAP size: " + openHeap.size());
        System.out.println("BOUND: " + lowerBound / (double) CostManager.SCALE_FACTOR
                           + " (found cost " + found / (double) CostManager.SCALE_FACTOR + ")");
        System.out.println(lessonCount);
    }
}