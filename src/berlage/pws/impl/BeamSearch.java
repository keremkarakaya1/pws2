package berlage.pws.impl;

import berlage.pws.model.ScheduleState;
import berlage.pws.model.SubjectGroup;
import berlage.pws.util.CostManager;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

import java.util.ArrayList;
import java.util.List;

public class BeamSearch implements Search {
    private final ScheduleState initialState = new ScheduleState(null, null, 0, 0, 0);
    private final ChildGenerator childGenerator;
    private final List<SubjectGroup> lessonInstances;
    private final List<ScheduleState> keptStates = new ArrayList<>();
    private final Heap heap = new Heap(false);
    private final int beamWidth;

    public BeamSearch(TestData data, int beamWidth, boolean heuristic) {
        childGenerator = new ChildGenerator(data, heuristic, CostManager.SCALE_FACTOR);
        lessonInstances = data.lessonInstances();
        this.beamWidth = beamWidth;
    }

    @Override
    public ScheduleState find() {
        childGenerator.reset();
        heap.clear();
        keptStates.clear();
        keptStates.add(initialState);

        for (int index = 0; index < lessonInstances.size(); index++) {
            for (ScheduleState state : keptStates) {
                for (ScheduleState child : childGenerator.generateChildren(state)) {
                    heap.offer(child);
                }
            }

            if (heap.isEmpty()) {
                throw new RuntimeException("Beam Search fail -- Increase Width");
            }

            System.out.println(index + " candidates: " + heap.size());
            keptStates.clear();

            long count = Math.min(beamWidth, heap.size());

            for (long i = 0; i < count; i++) {
                keptStates.add(heap.poll());
            }

            heap.clear();
        }

        // The heap polls best first, and h is zero at a goal, so the first kept state has the fewest gaps.
        return keptStates.get(0);
    }
}