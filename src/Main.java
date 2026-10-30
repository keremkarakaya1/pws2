import berlage.pws.impl.BeamSearch;
import berlage.pws.impl.BranchBoundDFS;
import berlage.pws.impl.PathFinder;
import berlage.pws.model.*;
import berlage.pws.util.*;

import java.util.*;

public class Main {

    //TODO testing -> new method in childgen new vars pathfinder ->

    private static void warmUp(TestData data) {
        boolean[][] settings = {
                //{false, false},   // heuristic, advanced
                {true, false},
                {true, true}
        };

        int warmUps = 3;

        for (int w = 0; w < warmUps; w++) {
            for (boolean[] s : settings) {
                new PathFinder(data, s[0], false, s[1]).find();
            }
        }

        System.out.println("warmup complete");

        for (int i = 0; i < 5; i++) {
            System.out.println();
        }
    }

    private static void runAstarBasic(PathFinder aStar, OutputManager outputManagerAStar, List<TestResult> results) {
        String name = "A* BASIC";

        long start = System.nanoTime();
        ScheduleState aStarGoal = aStar.find();
        long end = System.nanoTime();

        double runAverageAstar = OutputManager.getAverageGap(outputManagerAStar.getSchedules(aStarGoal));
        outputManagerAStar.outputSchedule(aStarGoal);

        results.add(new TestResult(name, runAverageAstar, aStarGoal.getTotalCost(), (end - start) / 1000000, (end - start) / 1000000000));
    }

    private static void runBeam(BeamSearch beam, OutputManager outputManagerBeam, List<TestResult> results) {
        String name = "Beam search";

        long start = System.nanoTime();
        ScheduleState beamGoal = beam.find();
        long end = System.nanoTime();

        double runAverage = OutputManager.getAverageGap(outputManagerBeam.getSchedules(beamGoal));
        outputManagerBeam.outputSchedule(beamGoal);

        results.add(new TestResult(name, runAverage, beamGoal.getTotalCost(), (end - start) / 1000000, (end - start) / 1000000000));
    }

    private static void runDijkstra(PathFinder dijkstra, OutputManager outputManagerDijkstra, List<TestResult> results) {
        String name = "DIJKSTRA";

        long start = System.nanoTime();
        ScheduleState dijkstraGoal = dijkstra.find();
        long end = System.nanoTime();


        double runAverage = OutputManager.getAverageGap(outputManagerDijkstra.getSchedules(dijkstraGoal));
        outputManagerDijkstra.outputSchedule(dijkstraGoal);

        results.add(new TestResult(name, runAverage, dijkstraGoal.getTotalCost(), (end - start) / 1000000, (end - start) / 1000000000));
    }

    private static void runAstarAdvanced(PathFinder aStarAdvanced, OutputManager outputManagerAstarAdvanced, List<TestResult> results) {
        String name = "A* ADVANCED";

        long start = System.nanoTime();
        ScheduleState goalStateAdvanced = aStarAdvanced.find();
        long end = System.nanoTime();

        double runAverageAdvanced = OutputManager.getAverageGap(outputManagerAstarAdvanced.getSchedules(goalStateAdvanced));
        outputManagerAstarAdvanced.outputSchedule(goalStateAdvanced);

        results.add(new TestResult(name, runAverageAdvanced, goalStateAdvanced.getTotalCost(), (end - start) / 1000000, (end - start) / 1000000000));
    }

    private static void runBasicBranchAndBoundWHeuristic(BranchBoundDFS branchBoundDFS, OutputManager outputManagerBB, List<TestResult> results) {
        String name = "Branch and Bound with heuristic";

        long start = System.nanoTime();
        ScheduleState goalStateBB = branchBoundDFS.find();
        long end = System.nanoTime();

        double runAverageAdvanced = OutputManager.getAverageGap(outputManagerBB.getSchedules(goalStateBB));
        outputManagerBB.outputSchedule(goalStateBB);

        results.add(new TestResult(name, runAverageAdvanced, goalStateBB.getTotalCost(), (end - start) / 1000000, (end - start) / 1000000000));
    }


    private static Set<Subject> combine(Set<Subject> common, Set<Subject> profile) {
        Set<Subject> result = new HashSet<>(common);
        result.addAll(profile);
        return Set.copyOf(result);
    }

    public static void main(String[] args) {
        final int classCapacity = 30;
        final int teacherCountPerSubject = 3;
        final int countRegular = 12;
        final int countLab = 2;
        final int countGym = 2;
        final int countStudents = 150;
        final int lessonsPerDayFull = 9;
        final int lessonsPerDayTiny = 6;
        final int maxCountSubjectPerDay = 2;
        final long seed = 12345;
        final int daysTiny = 3;
        final int daysFull = 5;

        //region shared constant sets of subjects and profiles
        Set<Subject> electives = EnumSet.of(
            Subject.FRANS, Subject.DUITS,
            Subject.WISKUNDE_A, Subject.WISKUNDE_B, Subject.WISKUNDE_C,
            Subject.NATUURKUNDE, Subject.SCHEIKUNDE, Subject.BIOLOGIE,
            Subject.GESCHIEDENIS, Subject.AARDRIJKSKUNDE,
            Subject.ECONOMIE, Subject.BEDRIJFSECONOMIE,
            Subject.MAATSCHAPPIJLEER, Subject.FILOSOFIE, Subject.KUNST
        );

        Set<Subject> commonMandatory = EnumSet.of(
            Subject.NEDERLANDS,
            Subject.ENGELS,
            Subject.LICHAMELIJKE_OPVOEDING,
            Subject.MAATSCHAPPIJLEER,
            Subject.CKV
        );

        Set<Subject> profileNT = EnumSet.of(
                Subject.WISKUNDE_B,
                Subject.NATUURKUNDE,
                Subject.SCHEIKUNDE
        );

        Set<Subject> profileNG = EnumSet.of(
                Subject.WISKUNDE_A,
                Subject.BIOLOGIE,
                Subject.SCHEIKUNDE
        );

        Set<Subject> profileEM = EnumSet.of(
                Subject.WISKUNDE_A,
                Subject.GESCHIEDENIS,
                Subject.ECONOMIE
        );

        Set<Subject> profileCM = EnumSet.of(
                Subject.WISKUNDE_C,
                Subject.GESCHIEDENIS
        );

        Profile EM = new Profile(0,0, combine(commonMandatory, profileEM));
        Profile CM = new Profile(1,0, combine(commonMandatory, profileCM));
        Profile NT = new Profile(2, 1, combine(commonMandatory, profileNT));
        Profile NG = new Profile(3, 1, combine(commonMandatory, profileNG));
        //endregion

        Map<Subject, Integer> requiredPerWeek = new EnumMap<>(Map.ofEntries(
                Map.entry(Subject.NEDERLANDS, 4),
                Map.entry(Subject.ENGELS, 3),
                Map.entry(Subject.FRANS, 2),
                Map.entry(Subject.DUITS, 2),
                Map.entry(Subject.WISKUNDE_A, 4),
                Map.entry(Subject.WISKUNDE_B, 4),
                Map.entry(Subject.WISKUNDE_C, 3),
                Map.entry(Subject.NATUURKUNDE, 3),
                Map.entry(Subject.SCHEIKUNDE, 3),
                Map.entry(Subject.BIOLOGIE, 3),
                Map.entry(Subject.GESCHIEDENIS, 2),
                Map.entry(Subject.AARDRIJKSKUNDE, 2),
                Map.entry(Subject.ECONOMIE, 3),
                Map.entry(Subject.BEDRIJFSECONOMIE, 3),
                Map.entry(Subject.MAATSCHAPPIJLEER, 2),
                Map.entry(Subject.CKV, 1),
                Map.entry(Subject.FILOSOFIE, 2),
                Map.entry(Subject.KUNST, 2),
                Map.entry(Subject.LICHAMELIJKE_OPVOEDING, 2)
        ));

        Map<Subject, Integer> requiredPerWeekTiny = new EnumMap<>(Map.ofEntries(
                Map.entry(Subject.NEDERLANDS, 1),
                Map.entry(Subject.ENGELS, 1),
                Map.entry(Subject.FRANS, 1),
                Map.entry(Subject.DUITS, 1),
                Map.entry(Subject.WISKUNDE_A, 1),
                Map.entry(Subject.WISKUNDE_B, 2),
                Map.entry(Subject.WISKUNDE_C, 1),
                Map.entry(Subject.NATUURKUNDE, 1),
                Map.entry(Subject.SCHEIKUNDE, 1),
                Map.entry(Subject.BIOLOGIE, 1),
                Map.entry(Subject.GESCHIEDENIS, 1),
                Map.entry(Subject.AARDRIJKSKUNDE, 1),
                Map.entry(Subject.ECONOMIE, 1),
                Map.entry(Subject.BEDRIJFSECONOMIE, 1),
                Map.entry(Subject.MAATSCHAPPIJLEER, 1),
                Map.entry(Subject.CKV, 1),
                Map.entry(Subject.FILOSOFIE, 1),
                Map.entry(Subject.KUNST, 1),
                Map.entry(Subject.LICHAMELIJKE_OPVOEDING, 1)
        ));

        Set<Profile> profiles = Set.of(EM, CM, NT, NG);


        Map<Profile, Integer> electiveCountPerProfile = Map.of(
            CM,2,
            EM,2,
            NG,1,
            NT,1
        );

        TestDataGenerator testDataGeneratorTiny = new TestDataGenerator(
                requiredPerWeekTiny,
                Map.of(CM,1,
                        EM,1,
                        NG,1,
                        NT,1),
                profiles,
                electives,
                seed,
                20,
                1,
                6,
                1,
                1,
                teacherCountPerSubject * 2,
                80,
                lessonsPerDayTiny,
                maxCountSubjectPerDay,
                daysTiny
        );

        TestDataGenerator testDataGenerator = new TestDataGenerator(
                requiredPerWeek,
                electiveCountPerProfile,
                profiles,
                electives,
                seed,
                classCapacity,
                teacherCountPerSubject,
                countRegular,
                countGym,
                countLab,
                teacherCountPerSubject * 2,
                countStudents,
                lessonsPerDayFull,
                maxCountSubjectPerDay,
                daysFull);

        TestData testData = testDataGenerator.build();
        System.out.println(testData.lessonInstances().size());

        TestData testDataTiny = testDataGeneratorTiny.build();
        System.out.println(testDataTiny.lessonInstances().size());

        new FileWriter().writeTestData(testData);
        System.out.println("WRITTEN");

        TestDataValidator.validate(testData);
        TestDataValidator.validate(testDataTiny);

        //OutputManager outputManagerBB = new OutputManager(lessonsPerDayFull, daysFull, "outputBB.html");
        //OutputManager outputManagerDijkstra = new OutputManager(lessonsPerDayTiny, daysTiny, "outputDijkstra.html");
        //OutputManager outputManagerDFS = new OutputManager(lessonsPerDayFull, daysFull, "outputDFS.html");
        //OutputManager outputManagerBeamWithH = new OutputManager(lessonsPerDayTiny, daysTiny, "outputBEAMwH.html");
        //OutputManager outputManagerAstarAdvanced = new OutputManager(lessonsPerDayTiny, daysTiny, "outputAstarAdvanced.html");
        //PathFinder aStarAdvanced = new PathFinder(testDataTiny, true, false, true);

        //PathFinder dfs = new PathFinder(testData, false, true, false);
        //PathFinder dijkstra = new PathFinder(testDataTiny, false, false, false);
        //BeamSearch beamSearch = new BeamSearch(testData, 100000, true, false);
        //BeamSearch beamSearchNoH = new BeamSearch(testData, 100000, false, false);
        //BeamSearch beamSearchFullH = new BeamSearch(testData, 100000, true, true);

        //BeamSearch beamSearchTinyWithH = new BeamSearch(testDataTiny, 100000, true, true);
        //beamSearch no heuristic is better
        //BranchBoundDFS branchBoundDFSAdvanced = new BranchBoundDFS(testDataTiny, 120, 100000, true, false, true);
        //BranchBoundDFS branchBoundDFSBasic = new BranchBoundDFS(testDataTiny, 120, 100000, true, false, false);

        ScheduleState dijkstraGoal;
        ScheduleState aStarGoal;
        ScheduleState goalStateAdvanced;
        ScheduleState goalBeam;
        ScheduleState goalDFS;
        ScheduleState goalBB;

        //warmUp(testDataTiny);
        List<TestResult> results = new ArrayList<>();

        //warmUp(testDataTiny);
        //runDijkstra(dijkstra, outputManagerDijkstra, results);
        //runBasicBranchAndBoundWHeuristic(branchBoundDFSAdvanced, outputManagerBB, results);
        //runBasicBranchAndBoundWHeuristic(branchBoundDFSBasic, outputManagerBB, results);


        /*
        PathFinder aStarWeighted = new PathFinder(testDataTiny, true, false, false, 2);
        PathFinder aStar = new PathFinder(testDataTiny, true, false, false);
        OutputManager outputManagerAStar = new OutputManager(lessonsPerDayTiny, daysTiny, "outputAstar.html");
        runAstarBasic(aStarWeighted, outputManagerAStar, results);
         */

        PathFinder aStarWeightedFull = new PathFinder(testData, true, false, false, 2);
        OutputManager outputManagerAStarWeightedFull = new OutputManager(lessonsPerDayFull, daysFull, "outputAstarWEIGHTED_FULL.html");
        runAstarBasic(aStarWeightedFull, outputManagerAStarWeightedFull, results);

        //runAstarAdvanced(aStarAdvanced, outputManagerAstarAdvanced, results);
        //runBeam(beamSearchTiny, outputManagerBeam, results);
        //runBeam(beamSearchTinyWithH, outputManagerBeamWithH, results);
        //runBeam(beamSearch, outputManagerBeam, results);

        //OutputManager outputManagerBeam = new OutputManager(lessonsPerDayFull, daysFull, "outputBEAM.html");
        //BeamSearch beamSearchTiny = new BeamSearch(testDataTiny, 100000, false, false);
        //BeamSearch beamSearchFull = new BeamSearch(testData, 2500000, false, false);
        //runBeam(beamSearchFull, outputManagerBeam, results);
        //runBeam(beamSearchFullH, outputManagerBeam, results);

        for (int i = 0; i < 3; i++) {
            System.out.println();
        }

        for (TestResult result : results) {
            System.out.println();
            System.out.println(result.name());
            System.out.println("Average gap count: " + result.averageGap());
            System.out.println("Cost: " + result.cost());
            System.out.println("runtimeMs: " + result.runtimeMilliseconds());
            System.out.println("runtimeSeconds: " + result.runtimeSeconds());
        }
    }
}