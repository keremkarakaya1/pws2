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
        childGenerator = new ChildGenerator(data, heuristic, advanced,100);
        initialState = new ScheduleState(null, null, 0, 0, 0);
        heap = new Heap(false);

        keptStates = new ArrayList<>();

        this.lessonInstances = data.lessonInstances();
        this.beamWidth = beamWidth;

    }

    @Override
    public ScheduleState find() {
        int index = initialState.getIndex();
        childGenerator.reset();
        heap.clear();
        keptStates.clear();
        keptStates.add(initialState);

        while (index < lessonInstances.size()) {
            System.out.println("cannotPlaceCount: " + childGenerator.cannotPlaceCount);
            System.out.println(index);

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

            long count = Math.min(beamWidth, heap.size());

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
