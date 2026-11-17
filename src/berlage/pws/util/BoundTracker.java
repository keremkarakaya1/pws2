package berlage.pws.util;

import berlage.pws.model.ScheduleState;
import berlage.pws.model.SubjectGroup;

import java.util.Arrays;
import java.util.List;

public class BoundTracker {
    private int[] counts;
    private final int weight;
    private final int offset;

    public BoundTracker(List<SubjectGroup> lessonInstances, int weight) {
        counts = new int[1024];
        offset = CostManager.getTheoreticalBest(lessonInstances);
        this.weight = weight;
    }

    public void track(ScheduleState state, boolean add) {
        int cost = state.getGCost() + state.getHCost() / weight * CostManager.SCALE_FACTOR;
        int index = (cost - offset) / (CostManager.GAP_WEIGHT * CostManager.SCALE_FACTOR);

        if (index < 0) {
            System.out.println("NEGATIVE INDEX");
            index = 0;
        }

        while (index >= counts.length) {
            resize();
        }

        if (add) {
            counts[index]++;
        } else {
            counts[index]--;
        }
    }

    public int getBound() {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                return i * CostManager.GAP_WEIGHT * CostManager.SCALE_FACTOR + offset;            }
        }

        return Integer.MAX_VALUE;
    }

    private void resize() {
        counts = Arrays.copyOf(counts, counts.length * 2);
    }

    public void reset() {
        Arrays.fill(counts,0);
    }
}
