package berlage.pws.model;

import java.util.Objects;
import java.util.Set;

public class Student {
    private final Profile profile;
    private final Set<Subject> electiveSubjects;
    private final int id;

    public Student(Profile profile, Set<Subject> electiveSubjects, int id) {
        this.profile = profile;
        this.electiveSubjects = electiveSubjects;
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public Set<Subject> getElectiveSubjects() {
        return electiveSubjects;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
