package berlage.pws.impl;

import berlage.pws.model.ScheduleState;
import berlage.pws.model.SubjectGroup;
import berlage.pws.util.CostManager;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

import java.util.*;

public class BranchBoundDFS implements Search{
    private final Heap heap;
    private final ChildGenerator childGenerator;
    private final ScheduleState initialState;
    private final List<SubjectGroup> lessonInstances;
    private final ScheduleState seedState;
    private final int timeLimitInSeconds;

    public BranchBoundDFS(TestData data,
                          int timeLimitInSeconds,
                          boolean heuristic,
                          boolean advanced) {
        this(data, timeLimitInSeconds, heuristic, false, advanced, null);
    }


    public BranchBoundDFS(TestData data,
                          int timeLimitInSeconds,
                          boolean heuristic,
                          boolean seeded,
                          boolean advanced,
                          Search algorithm) {

        if (seeded && algorithm == null) {
            throw new RuntimeException("SEARCH ALG NULL -> BB");
        }

        heap = new Heap(true);
        childGenerator = new ChildGenerator(data, heuristic, advanced, 1);
        initialState = new ScheduleState(null, null, 0,0,0);
        seedState = seeded ? algorithm.findOrDefault(initialState) : null;

        this.lessonInstances = childGenerator.getLessonInstances();
        this.timeLimitInSeconds = timeLimitInSeconds;
    }

    @Override
    public ScheduleState find() {
        heap.clear();
        childGenerator.reset();
        ScheduleState best = seedState == null ? initialState : seedState;
        int bestCost = seedState == null || seedState == initialState ? Integer.MAX_VALUE : seedState.getTotalCost();
        System.out.println("BEST COST: " + bestCost);

        long start = System.nanoTime();
        int theoreticalBest = CostManager.getTheoreticalBest(lessonInstances);
        System.out.println(theoreticalBest);

        heap.offer(initialState);
        System.out.println("SEARCH START");

        while (!heap.isEmpty()) {
            if ((System.nanoTime() - start) / 1000000000 >= timeLimitInSeconds) {
                if (bestCost == Integer.MAX_VALUE) {
                    throw new RuntimeException("NO SCHEDULE FOUND WITHIN TIME LIMIT");
                }

                System.out.println("Found schedule is NOT proven optimal");
                return best;
            }

            if (bestCost == theoreticalBest) {
                System.out.println("Best schedule found");
                return best;
            }

            ScheduleState state = heap.poll();

            if (state.getIndex() == lessonInstances.size()) {
                if (state.getTotalCost() < bestCost) {
                    bestCost = state.getTotalCost();
                    best = state;

                    System.out.println("new best cost:" + bestCost);
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

        if (bestCost == Integer.MAX_VALUE) {
            System.out.println("NO SCHEDULE FOUND");
            throw new RuntimeException();
        }

        return best;
    }
}
