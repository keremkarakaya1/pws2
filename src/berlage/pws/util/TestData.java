package berlage.pws.util;

import berlage.pws.impl.ZobristHashing;
import berlage.pws.model.*;

import java.util.*;

public record TestData(
        Map<RoomType, List<Room>> roomMap,
        Map<Subject, Set<Teacher>> teacherPool,
        Map<RoomType,Integer> countPerRoomType,
        Set<Student> students,
        Map<Subject, Set<SubjectGroup>> subjectGroups,
        Map<Subject, Integer> requiredPerWeek,
        Slot[] slots,
        int roomCount,
        int teacherCount,
        int lessonsPerDay,
        int days,
        int maxSameSubjectCountPerDay,
        ZobristHashing zobristHashing) {}