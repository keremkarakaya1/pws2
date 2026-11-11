package berlage.pws.util;

import berlage.pws.model.*;

import java.util.*;

public record TestData(
        Map<RoomType, Room[]> roomMap,
        Map<Subject, Set<Teacher>> teacherPool,
        Map<RoomType,Integer> countPerRoomType,
        Set<Student> students,
        Map<Subject, Set<SubjectGroup>> subjectGroups,
        Map<Subject, Integer> requiredPerWeek,
        Map<Teacher, int[]> studentsOfTeacherMap,
        List<SubjectGroup> lessonInstances,
        List<List<SubjectGroup>> subjectGroupsPerStudent,
        Slot[] slots,
        int[][] countGroupsToBeScheduledAtIndex,
        int[][] lessonsLeft,
        int groupCount,
        int roomCount,
        int teacherCount,
        int lessonsPerDay,
        int days,
        int maxSameSubjectCountPerDay) {}