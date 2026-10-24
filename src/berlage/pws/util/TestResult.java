package berlage.pws.util;

public record TestResult(String name,
                         double averageGap,
                         int cost,
                         long runtimeMilliseconds,
                         long runtimeSeconds) {}
