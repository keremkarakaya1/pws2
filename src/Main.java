import berlage.pws.model.Profile;
import berlage.pws.model.Subject;
import berlage.pws.util.TestData;
import berlage.pws.util.TestDataGenerator;

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
        final int countStudents = 220;
        final int lessonsPerDay = 9;
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
                lessonsPerDay);

        TestData testData = testDataGenerator.build();
    }
}