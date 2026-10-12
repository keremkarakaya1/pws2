package berlage.pws.impl;

import berlage.pws.model.ScheduleState;
import berlage.pws.model.SubjectGroup;
import berlage.pws.util.Heap;
import berlage.pws.util.Occupancy;
import berlage.pws.util.TestData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeamSearch implements Search{
    private final ScheduleState initialState;
    private final ChildGenerator childGenerator;
    private final List<SubjectGroup> lessonInstances;
    private final Heap keptStates;
    private final Heap heap;
    private final Occupancy occupancy;
    private final int beamWidth;

    public BeamSearch(List<SubjectGroup> lessonInstances, TestData data, int beamWidth) {
        childGenerator = new ChildGenerator(lessonInstances, data);
        initialState = new ScheduleState(null, null, 0, 0, 0, 0);
        heap = new Heap(false);

        keptStates = new Heap(false);
        keptStates.offer(initialState);

        this.occupancy = childGenerator.getOccupancy();
        this.lessonInstances = lessonInstances;
        this.beamWidth = beamWidth;

    }

    @Override
    public ScheduleState find() {
        int index = initialState.getIndex();

        while (index < lessonInstances.size()) {
            System.out.println(index);

            while (!keptStates.isEmpty()) {
                List<ScheduleState> childStates = childGenerator.generateChildren(keptStates.poll());

                for (ScheduleState scheduleState : childStates) {
                    heap.offer(scheduleState);
                }
            }

            if (heap.isEmpty()) {
                throw new RuntimeException("Beam Search fail -- Increase Width");
            }

            for (int i = 0; i < Math.min(beamWidth, heap.size()); i++) {
                keptStates.offer(heap.poll());
            }

            heap.clear();
            index++;
        }

        if (keptStates.isEmpty()) {
            throw new RuntimeException("BEAM SEARCH FAIL -- NULL SCHEDULE, Increase Width");
        }

        return keptStates.poll();
    }
}
