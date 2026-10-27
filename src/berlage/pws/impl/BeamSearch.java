package berlage.pws.impl;

import berlage.pws.model.ScheduleState;
import berlage.pws.model.SubjectGroup;
import berlage.pws.util.Heap;
import berlage.pws.util.TestData;

import java.util.ArrayList;
import java.util.List;

public class BeamSearch implements Search{
    private final ScheduleState initialState;
    private final ChildGenerator childGenerator;
    private final List<SubjectGroup> lessonInstances;
    private final List<ScheduleState> keptStates;
    private final Heap heap;
    private final int beamWidth;

    public BeamSearch(TestData data, int beamWidth, boolean heuristic, boolean advanced) {
        childGenerator = new ChildGenerator(data, true, true, heuristic, advanced);
        initialState = new ScheduleState(null, null, 0, 0, 0, 0);
        heap = new Heap(false);

        keptStates = new ArrayList<>();
        keptStates.add(initialState);

        this.lessonInstances = data.lessonInstances();
        this.beamWidth = beamWidth;

    }

    @Override
    public ScheduleState find() {
        int index = initialState.getIndex();

        while (index < lessonInstances.size()) {
            System.out.println("cannotPlaceCount: " + childGenerator.cannotPlaceCount);
            System.out.println(index);
            childGenerator.clearSet();

            for (ScheduleState state : keptStates) {
                List<ScheduleState> childStates = childGenerator.generateChildren(state);

                for (ScheduleState scheduleState : childStates) {
                    heap.offer(scheduleState);
                }
            }

            keptStates.clear();

            System.out.println("HEAPSIZE: " + heap.size());

            if (heap.isEmpty()) {
                throw new RuntimeException("Beam Search fail -- Increase Width");
            }

            int count = Math.min(beamWidth, heap.size());

            for (int i = 0; i < count; i++) {
                keptStates.add(heap.poll());
            }

            heap.clear();
            index++;
        }

        int currentCost = Integer.MAX_VALUE;
        ScheduleState result = null;

        for (ScheduleState state : keptStates) {
            if (state.getTotalCost() < currentCost) {
                currentCost = state.getTotalCost();
                result = state;
            }
        }

        return result;
    }
}
