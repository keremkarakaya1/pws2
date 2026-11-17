package berlage.pws.util;

/**
 * Cost unit: SCALE_FACTOR per gap. Divide a cost by SCALE_FACTOR to get a gap count.
 * g is charged SCALE_FACTOR per dead hole; h is charged the scaled weight per forced dead hole.
 */
public final class CostManager {
    public static final int SCALE_FACTOR = 100;

    private CostManager() {}

    public static int convertWeight(double weight) {
        return (int) Math.round(weight * SCALE_FACTOR);
    }
}