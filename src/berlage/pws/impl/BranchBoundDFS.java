package berlage.pws.impl;

import berlage.pws.model.ScheduleState;
import berlage.pws.util.CostManager;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

import java.util.List;

public class BranchBoundDFS implements Search {
    private final Heap heap = new Heap(true);
    private final ChildGenerator childGenerator;
    private final ScheduleState initialState = new ScheduleState(null, null, 0, 0, 0);
    private final ScheduleState seedState;
    private final int lessonCount;
    private final long timeLimitNanos;

    public BranchBoundDFS(TestData data, int timeLimitInSeconds, boolean heuristic) {
        this(data, timeLimitInSeconds, heuristic, null);
    }

    /** A non-null seed search gives a starting upper bound. */
    public BranchBoundDFS(TestData data, int timeLimitInSeconds, boolean heuristic, Search seedSearch) {
        childGenerator = new ChildGenerator(data, heuristic, CostManager.SCALE_FACTOR);
        seedState = seedSearch == null ? null : seedSearch.findOrDefault(null);
        lessonCount = childGenerator.getLessonInstances().size();
        timeLimitNanos = timeLimitInSeconds * 1_000_000_000L;
    }

    @Override
    public ScheduleState find() {
        heap.clear();
        childGenerator.reset();

        ScheduleState best = seedState;
        int bestCost = best == null ? Integer.MAX_VALUE : best.getTotalCost();
        long start = System.nanoTime();

        heap.offer(initialState);

        while (!heap.isEmpty()) {
            if (System.nanoTime() - start >= timeLimitNanos) {
                if (best == null) {
                    throw new RuntimeException("NO SCHEDULE FOUND WITHIN TIME LIMIT");
                }

                System.out.println("Found schedule is NOT proven optimal");
                return best;
            }

            // Zero gaps cannot be beaten.
            if (bestCost == 0) {
                System.out.println("Best schedule found");
                return best;
            }

            ScheduleState state = heap.poll();

            if (state.getIndex() == lessonCount) {
                if (state.getTotalCost() < bestCost) {
                    best = state;
                    bestCost = state.getTotalCost();
                    System.out.println("new best gaps: " + bestCost / (double) CostManager.SCALE_FACTOR);
                }

                continue;
            }

            if (state.getTotalCost() >= bestCost) {
                continue;
            }

            List<ScheduleState> children = childGenerator.generateChildren(state);

            for (ScheduleState child : children) {
                if (child.getTotalCost() < bestCost) {
                    heap.offer(child);
                }
            }
        }

        if (best == null) {
            throw new RuntimeException("NO SCHEDULE FOUND");
        }

        return best;
    }
}