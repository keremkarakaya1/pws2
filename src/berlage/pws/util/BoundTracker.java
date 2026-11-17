package berlage.pws.util;

import berlage.pws.model.ScheduleState;

import java.util.Arrays;

/** Tracks the unweighted f (in whole gaps) of all open states; the minimum is a lower bound on the optimum. */
public class BoundTracker {
    private int[] counts = new int[1024];
    private final int weight;

    public BoundTracker(int weight) {
        this.weight = weight;
    }

    public void track(ScheduleState state, boolean add) {
        int index = state.getGCost() / CostManager.SCALE_FACTOR + state.getHCost() / weight;

        if (index >= counts.length) {
            counts = Arrays.copyOf(counts, Math.max(index + 1, counts.length * 2));
        }

        counts[index] += add ? 1 : -1;
    }

    public int getBound() {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0) {
                return i * CostManager.SCALE_FACTOR;
            }
        }

        return Integer.MAX_VALUE;
    }

    public void reset() {
        Arrays.fill(counts, 0);
    }
}