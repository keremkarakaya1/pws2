package berlage.pws.model;

import java.util.Set;

public class Profile {
    private final Set<Subject> mandatorySubjects;

    public Profile(Set<Subject> mandatorySubjects) {
        this.mandatorySubjects = mandatorySubjects;
    }
}
