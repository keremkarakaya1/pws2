package berlage.pws.model;

import java.util.Objects;
import java.util.Set;

public class SubjectGroup {
    private final Teacher teacher;
    private final Subject subject;
    private final Set<Student> set;
    private final int id;

    public SubjectGroup(Teacher teacher, Subject subject, Set<Student> set, int id) {
        this.teacher = teacher;
        this.subject = subject;
        this.set = set;
        this.id = id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Subject getSubject() {
        return subject;
    }

    public Set<Student> getStudents() {
        return set;
    }


    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SubjectGroup group = (SubjectGroup) o;
        return id == group.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "id " + id + " teacherID: " +  teacher + " Subject: " + subject;
    }
}
