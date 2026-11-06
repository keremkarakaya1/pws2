package berlage.pws.util;

public record TestResult(String name,
                         double averageGap,
                         int cost,
                         int lowerBound,
                         long runtimeMilliseconds,
                         long runtimeSeconds) {}
