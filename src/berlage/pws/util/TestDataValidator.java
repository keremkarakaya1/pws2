package berlage.pws.util;

import berlage.pws.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TestDataValidator {
    private TestDataValidator() {}

    public static void validate(TestData testData) {
        validateCounts(testData);
        validateTeachers(testData);
        validateIDs(testData);
        validateTables(testData);
        validateStudents(testData);



        //TODO finish post validator --Per-student list size, Instances with unknown groups, Loads.

    }

    private static void validateCounts(TestData data) {
        int groupCount = data.groupCount();
        int roomCount = data.roomCount();
        int teacherCount = data.teacherCount();

        Set<SubjectGroup> allGroups = new HashSet<>();

        for (Set<SubjectGroup> groups : data.subjectGroups().values()) {
            allGroups.addAll(groups);
        }

        if (groupCount != allGroups.size()) {
            throw new IllegalArgumentException("Post validation fail: groupCount does not match with actual size");
        }

        Set<Teacher> allTeachers = new HashSet<>();

        for (Set<Teacher> teachers : data.teacherPool().values()) {
            allTeachers.addAll(teachers);
        }

        if (teacherCount != allTeachers.size()) {
            throw new IllegalArgumentException("Post validation fail: teacherCount does not match with actual size");
        }

        int allRooms = 0;

        for (List<Room> list : data.roomMap().values()) {
            allRooms += list.size();
        }

        if (allRooms != roomCount) {
            throw new IllegalArgumentException("Post validation fail: roomCount does not match with actual size");
        }
    }

    private static void validateStudents(TestData data) {
        int days = data.days();
        int lessonsPerDay = data.lessonsPerDay();
        Set<Student> students = data.students();
        List<List<SubjectGroup>> subjectGroupsPerStudent = data.subjectGroupsPerStudent();
        Map<Subject, Integer> requiredPerWeek = data.requiredPerWeek();

        int totalAvailableSlots = days * lessonsPerDay;

        for (Student student : students) {
            List<SubjectGroup> groups = subjectGroupsPerStudent.get(student.getId());
            int total = 0;

            for (SubjectGroup group : groups) {
                total += requiredPerWeek.get(group.getSubject());
            }

            if (total > totalAvailableSlots) {
                throw new IllegalArgumentException("Post validation fail: Student " + student + " groups cannot fit in student's week " + total + "/" + totalAvailableSlots +  " increase days in a week, or reduce required per week");
            }
        }

    }

    private static void validateTables(TestData testData) {
        List<SubjectGroup> instances = testData.lessonInstances();
        int[][] lessonsLeft = testData.lessonsLeft();
        int[][] groupsLeft = testData.countGroupsToBeScheduledAtIndex();
        Set<Student> students = testData.students();
        int positions = instances.size() + 1;

        if (lessonsLeft.length != students.size()) {
            throw new IllegalArgumentException("Post validation fail: lessons left table has " + lessonsLeft.length + " rows for " + students.size() + " students");
        }

        if (groupsLeft.length != testData.groupCount()) {
            throw new IllegalArgumentException("Post validation fail: group count table has " + groupsLeft.length + " rows for " + testData.groupCount() + " groups");
        }

        for (int[] row : lessonsLeft) {
            if (row.length != positions) {
                throw new IllegalArgumentException("Post validation fail: lessons left row length " + row.length + ", expected " + positions);
            }
        }

        for (int[] row : groupsLeft) {
            if (row.length != positions) {
                throw new IllegalArgumentException("Post validation fail: group count row length " + row.length + ", expected " + positions);
            }
        }

        int[] studentRemaining = new int[students.size()];
        int[] groupRemaining = new int[testData.groupCount()];

        for (SubjectGroup group : instances) {
            groupRemaining[group.getId()]++;

            for (Student student : group.getStudents()) {
                studentRemaining[student.getId()]++;
            }
        }

        for (int position = 0; position < positions; position++) {
            for (Student student : students) {
                int id = student.getId();

                if (lessonsLeft[id][position] != studentRemaining[id]) {
                    throw new IllegalArgumentException("Post validation fail: lessons left table, student " + id + " at position " + position + " is " + lessonsLeft[id][position] + ", expected " + studentRemaining[id]);
                }
            }

            for (int id = 0; id < groupRemaining.length; id++) {
                if (groupsLeft[id][position] != groupRemaining[id]) {
                    throw new IllegalArgumentException("Post validation fail: group count table, group " + id + " at position " + position + " is " + groupsLeft[id][position] + ", expected " + groupRemaining[id]);
                }
            }

            if (position < instances.size()) {
                SubjectGroup placed = instances.get(position);
                groupRemaining[placed.getId()]--;

                for (Student student : placed.getStudents()) {
                    studentRemaining[student.getId()]--;
                }
            }
        }
    }

    private static void validateTeachers(TestData data) {
        for (Set<Teacher> teachers : data.teacherPool().values()) {
            for (Teacher teacher : teachers) {
                Set<Subject> subjects = teacher.getSubjects();
                int availableDayCount = countAvailableDays(teacher);

                for (Subject subject : subjects) {
                    if (data.maxSameSubjectCountPerDay() * availableDayCount < data.requiredPerWeek().get(subject)) {
                        throw new IllegalArgumentException("Post validation fail: Teacher " + teacher + " (" + availableDayCount + " days) can't teach any of their groups");
                    }
                }
            }
        }

    }

    private static void validateIDs(TestData data) {
        boolean[] studentsArray = new boolean[data.students().size()];
        boolean[] roomsArray = new boolean[data.roomCount()];
        boolean[] teachersArray = new boolean[data.teacherCount()];
        boolean[] groupsArray = new boolean[data.groupCount()];

        Set<SubjectGroup> allGroups = new HashSet<>();

        for (Set<SubjectGroup> groups : data.subjectGroups().values()) {
            allGroups.addAll(groups);
        }

        for (SubjectGroup group : allGroups) {
            if (group.getId() < 0 || group.getId() >= groupsArray.length) {
                throw new IllegalArgumentException("Post validation fail: Group IDs contains gaps or is less than 0 " + group.getId());
            }

            if (groupsArray[group.getId()]) {
                throw new IllegalArgumentException("Post validation fail: Group ID used by multiple groups " + group.getId());
            }

            groupsArray[group.getId()] = true;
        }


        Set<Teacher> allTeachers = new HashSet<>();

        for (Set<Teacher> teachers : data.teacherPool().values()) {
            allTeachers.addAll(teachers);
        }

        for (Teacher teacher : allTeachers) {
            if (teacher.getId() < 0 || teacher.getId() >= teachersArray.length) {
                throw new IllegalArgumentException("Post validation fail: Teacher IDs contains gaps or is less than 0 " + teacher.getId());
            }

            if (teachersArray[teacher.getId()]) {
                throw new IllegalArgumentException("Post validation fail: Teacher ID used by multiple teachers " + teacher.getId());
            }

            teachersArray[teacher.getId()] = true;
        }

        for (List<Room> roomList : data.roomMap().values()) {
            for (Room room : roomList) {
                if (room.getId() < 0 || room.getId() >= roomsArray.length) {
                    throw new IllegalArgumentException("Post validation fail: Room IDs contain gaps " + room.getId());
                }

                if (roomsArray[room.getId()]) {
                    throw new IllegalArgumentException("Post validation fail: Room ID used by multiple rooms " + room.getId());
                }

                roomsArray[room.getId()] = true;
            }
        }

        for (Student student : data.students()) {
            if (student.getId() < 0 || student.getId() >= studentsArray.length) {
                throw new IllegalArgumentException("Post validation fail: Student IDs contain gaps " + student.getId());
            }

            if (studentsArray[student.getId()]) {
                throw new IllegalArgumentException("Post validation fail: Student ID used by multiple students " + student.getId());
            }

            studentsArray[student.getId()] = true;
        }
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
