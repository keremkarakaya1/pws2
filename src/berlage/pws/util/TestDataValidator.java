package berlage.pws.util;

import berlage.pws.impl.ZobristHashing;
import berlage.pws.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TestDataValidator {
    private TestDataValidator() {}

    public static void validate(TestData testData) {
        Map<Subject, Set<Teacher>> teacherPool = testData.teacherPool();
        Map<RoomType,Integer> countPerRoomType = testData.countPerRoomType();
        Set<Student> students = testData.students();
        Map<Subject, Set<SubjectGroup>> subjectGroups = testData.subjectGroups();
        Map<Subject, Integer> requiredPerWeek = testData.requiredPerWeek();
        int lessonsPerDay = testData.lessonsPerDay();
        int days = testData.days();
        int maxCountPerDay = testData.maxSameSubjectCountPerDay();
        ZobristHashing zobristHashing = testData.zobristHashing();

        for (Set<Teacher> teachers : teacherPool.values()) {
            for (Teacher teacher : teachers) {
                Set<Subject> subjects = teacher.getSubjects();
                int availableDayCount = countAvailableDays(teacher);

                for (Subject subject : subjects) {
                    if (maxCountPerDay * availableDayCount < requiredPerWeek.get(subject)) {
                        throw new IllegalArgumentException("Post validation fail: Teacher " + teacher + " (" + availableDayCount + " days) can't teach any of their groups");
                    }
                }

              
            }
        }

        //TODO finish post validator

    }

    private static int countAvailableDays(Teacher teacher) {
        boolean[] avail = teacher.getDefaultAvailable();
        int res = 0;

        for (boolean b : avail) {
            if (b) {
                res++;
            }
        }

        return res;
    }
}
