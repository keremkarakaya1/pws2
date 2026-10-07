import berlage.pws.impl.LessonInstanceBuilder;
import berlage.pws.impl.PathFinder;
import berlage.pws.model.Profile;
import berlage.pws.model.ScheduleState;
import berlage.pws.model.Subject;
import berlage.pws.model.SubjectGroup;
import berlage.pws.util.OutputManager;
import berlage.pws.util.TestData;
import berlage.pws.util.TestDataGenerator;
import berlage.pws.util.TestDataValidator;

import java.util.*;

public class Main {

    private static Set<Subject> combine(Set<Subject> common, Set<Subject> profile) {
        Set<Subject> result = new HashSet<>(common);
        result.addAll(profile);
        return Set.copyOf(result);
    }

    public static void main(String[] args) {
        final int classCapacity = 30;
        final int teacherCountPerSubject = 2;
        final int countRegular = 12;
        final int countLab = 1;
        final int countGym = 1;
        final int countStudents = 30;
        final int lessonsPerDay = 9;
        final int maxCountSubjectPerDay = 2;
        final long seed = 12345;

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

        Set<Profile> profiles = Set.of(EM, CM, NT, NG);

        Map<Profile, Integer> electiveCountPerProfile = Map.of(
            CM,2,
            EM,2,
            NG,1,
            NT,1
        );

        TestDataGenerator testDataGenerator = new TestDataGenerator(
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
                lessonsPerDay,
                maxCountSubjectPerDay);

        TestData testData = testDataGenerator.build();
        List<SubjectGroup> groups = LessonInstanceBuilder.build(testData.countPerRoomType(), testData.subjectGroups(), testData.requiredPerWeek());

        HashMap<Integer, Integer> load = new HashMap<>();
        for (SubjectGroup group : groups) {
            load.put(group.getTeacher().getId(), load.getOrDefault(group.getTeacher().getId(), 0) + 1);
        }

        for (Map.Entry<Integer, Integer> entry  : load.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

        TestDataValidator.validate(testData);
        OutputManager outputManager = new OutputManager(lessonsPerDay, 5, "output.html");
        PathFinder pathFinder = new PathFinder(false, testData, groups);
        ScheduleState goalState = pathFinder.find();

        if (goalState == null) {
            throw new RuntimeException("NULL Schedule");
        }

        outputManager.outputSchedule(goalState);


    }
}