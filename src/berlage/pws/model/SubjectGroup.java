package berlage.pws.model;

import java.util.Set;

public class SubjectGroup {
    private final Teacher teacher;
    private final Subject subject;
    private final Set<Student> set;

    public SubjectGroup(Teacher teacher, Subject subject, Set<Student> set) {
        this.teacher = teacher;
        this.subject = subject;
        this.set = set;
    }
}
