package berlage.pws.model;

import java.util.Objects;
import java.util.Set;

public class Profile {
    private final int id;
    private final int secondaryId;
    private final Set<Subject> mandatorySubjects;

    public Profile(int id, int secondaryId, Set<Subject> mandatorySubjects) {
        this.id = id;
        this.secondaryId = secondaryId;
        this.mandatorySubjects = mandatorySubjects;
    }

    public Set<Subject> getMandatorySubjects() {
        return mandatorySubjects;
    }

    public int getId() {
        return id;
    }

    public int getSecondaryId() {
        return secondaryId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return id == profile.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
