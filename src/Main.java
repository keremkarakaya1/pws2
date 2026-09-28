import berlage.pws.model.Profile;
import berlage.pws.model.Subject;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Set<Subject> commonMandatory = Set.of(
            Subject.NEDERLANDS,
            Subject.ENGELS,
            Subject.LICHAMELIJKE_OPVOEDING,
            Subject.MAATSCHAPPIJLEER,
            Subject.CKV
        );

        Set<Subject> profileNT = Set.of(
                Subject.WISKUNDE_B,
                Subject.NATUURKUNDE,
                Subject.SCHEIKUNDE
        );

        Set<Subject> profileNG = Set.of(
                Subject.WISKUNDE_A,
                Subject.BIOLOGIE,
                Subject.SCHEIKUNDE
        );

        Set<Subject> profileEM = Set.of(
                Subject.WISKUNDE_A,
                Subject.GESCHIEDENIS,
                Subject.ECONOMIE
        );

        Set<Subject> profileCM = Set.of(
                Subject.WISKUNDE_C,
                Subject.GESCHIEDENIS
        );

        Profile EM = new Profile(0,0, profileEM);
        Profile CM = new Profile(1,0, profileCM);
        Profile NT = new Profile(2, 1, profileNT);
        Profile NG = new Profile(3, 1, profileNG);
    }
}