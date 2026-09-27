package berlage.pws.model;

import java.util.Set;

public class Student {
    private final Profile profile;
    private final Set<Subject> electiveSubjects;

    public Student(Profile profile, Set<Subject> electiveSubjects) {
        this.profile = profile;
        this.electiveSubjects = electiveSubjects;
    }
}
