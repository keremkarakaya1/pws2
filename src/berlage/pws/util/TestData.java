package berlage.pws.util;

import berlage.pws.impl.ZobristHashing;
import berlage.pws.model.*;

import java.util.*;

public record TestData(
        List<Room> rooms,
        Map<Subject, Set<Teacher>> teacherPool,
        Map<RoomType,Integer> countPerRoomType,
        Set<Student> students,
        Map<Subject, Set<SubjectGroup>> subjectGroups,
        Map<Subject, Integer> requiredPerWeek,
        int lessonsPerDay,
        int days,
        ZobristHashing zobristHashing) {}