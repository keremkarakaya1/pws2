package berlage.pws.impl;

import berlage.pws.model.ScheduleState;
import berlage.pws.model.SubjectGroup;
import berlage.pws.util.CostManager;
import berlage.pws.util.Heap;
import berlage.pws.util.OutputManager;
import berlage.pws.util.TestData;

import java.util.*;

public class BranchBoundDFS implements Search{
    private final Heap heap;
    private final ChildGenerator childGenerator;
    //private final Set<Long> closedSet;
    private final ScheduleState initialState;
    private final List<SubjectGroup> lessonInstances;
    private final OutputManager outputManager;
    private final TestData data;
    private final int beamWidth;
    private final int timeLimitInSeconds;
    private final boolean beamStart;

    public BranchBoundDFS(TestData data,
                          int timeLimitInSeconds,
                          int beamWidth,
                          boolean heuristic,
                          boolean beamStart,
                          boolean advanced) {

        heap = new Heap(true);
        outputManager = new OutputManager(data.lessonsPerDay(), data.days());
        childGenerator = new ChildGenerator(data, true, true, heuristic, advanced, 1);
        initialState = new ScheduleState(null, null, 0,0,0,0);
        //closedSet = new HashSet<>();

        this.beamWidth = beamWidth;
        this.data = data;
        this.lessonInstances = childGenerator.getLessonInstances();
        this.timeLimitInSeconds = timeLimitInSeconds;
        this.beamStart = beamStart;


    }

    @Override
    public ScheduleState find() {
        heap.clear();

        int bestCost = Integer.MAX_VALUE;
        ScheduleState beamBaseline = initialState;

        if (beamStart) {
            BeamSearch beamSearch = new BeamSearch(data, beamWidth, false, false);
            beamBaseline = beamSearch.find();
            bestCost = Math.min(bestCost, CostManager.convertBeamCost(outputManager.getSchedules(beamBaseline)));
            System.out.println("BEST COST: " + bestCost);
        }

        long start = System.nanoTime();
        //int iterationCount = 0;
        int theoreticalBest = CostManager.getTheoreticalBest(lessonInstances);

        ScheduleState best = beamStart ? beamBaseline : initialState;

        heap.offer(initialState);
        System.out.println("SEARCH START");

        while (!heap.isEmpty()) {
            //iterationCount++;

            /*
            if (iterationCount % 100000 == 0) {
                System.out.println(Runtime.getRuntime().totalMemory());
            }
             */

            if ((System.nanoTime() - start) / 1000000000 >= timeLimitInSeconds) {
                if (bestCost == Integer.MAX_VALUE) {
                    throw new RuntimeException("NO SCHEDULE FOUND WITHIN TIME LIMIT");
                }

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

            if (bestCost == theoreticalBest) {
                return best;
            }

            if (state.getTotalCost() >= bestCost) {
                continue;
            }

            /*
            if (!closedSet.add(state.getHash())) {
                continue;
            }
             */

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
