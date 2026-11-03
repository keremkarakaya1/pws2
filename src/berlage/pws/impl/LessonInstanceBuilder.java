package berlage.pws.impl;

import berlage.pws.model.*;

import java.util.*;

public final class LessonInstanceBuilder {

    private LessonInstanceBuilder() {}

    public static List<SubjectGroup> build(Map<RoomType, Integer> roomCountPerType, Map<Subject, Set<SubjectGroup>> groups, Map<Subject, Integer> requiredPerWeek) {
        List<SubjectGroup> flat = new ArrayList<>();
        List<SubjectGroup> lessonInstances = new ArrayList<>();

        for (Set<SubjectGroup> set: groups.values()) {
            flat.addAll(set);
        }

        Set<SubjectGroup> set = new HashSet<>(flat);

        Map<SubjectGroup, Integer> overlapMap = getOverlap(set);
        Map<Teacher, Integer> teacherAvailability = getAvailableDaysPerTeacher(set);
        Map<Teacher, Integer> groupCountPerTeacher = getGroupCountPerTeacher(set);

        Comparator<SubjectGroup> comparator = Comparator
                .comparingInt((SubjectGroup group) -> teacherAvailability.get(group.getTeacher()))
                .thenComparingInt(group -> roomCountPerType.get(group.getTeacher().getTypeRequirement()))
                .thenComparingInt(group -> -overlapMap.get(group))
                .thenComparingDouble(group -> -(double) groupCountPerTeacher.get(group.getTeacher()) / teacherAvailability.get(group.getTeacher()))
                .thenComparingInt(SubjectGroup::getId);


        flat.sort(comparator);

        for (SubjectGroup group : flat) {
            Subject subject = group.getSubject();

            if (!requiredPerWeek.containsKey(subject)) {
                throw new IllegalStateException(subject + " missing -- InstanceBuilder");
            }

            int count = requiredPerWeek.get(subject);

            for (int i = 0; i < count; i++) {
                lessonInstances.add(group);
            }
        }

        return lessonInstances;
    }

    private static Map<SubjectGroup, Integer> getOverlap(Set<SubjectGroup> set) {
        Map<Student, List<SubjectGroup>> groupsPerStudent = getStudentGroupList(set);
        Map<SubjectGroup, Integer> result = new HashMap<>();

        for (SubjectGroup group : set) {
            Set<SubjectGroup> others = new HashSet<>();

            for (Student student : group.getStudentsSet()) {
                others.addAll(groupsPerStudent.get(student));
            }

            others.remove(group);
            result.put(group, others.size());
        }

        return result;
    }

    private static Map<Student, List<SubjectGroup>> getStudentGroupList(Set<SubjectGroup> set) {
        Map<Student, List<SubjectGroup>> result = new HashMap<>();

        for (SubjectGroup group : set) {
            for (Student student : group.getStudentsSet()) {
                result.computeIfAbsent(student, k -> new ArrayList<>()).add(group);
            }
        }

        return result;
    }

    private static Map<Teacher,Integer> getGroupCountPerTeacher(Set<SubjectGroup> set) {
        Map<Teacher, Integer> result = new HashMap<>();

        for (SubjectGroup group : set) {
            Teacher teacher = group.getTeacher();
            result.put(teacher, result.getOrDefault(teacher, 0) + 1);
        }

        return result;
    }

    private static Map<Teacher, Integer> getAvailableDaysPerTeacher(Set<SubjectGroup> set) {
        Map<Teacher, Integer> result = new HashMap<>();

        for (SubjectGroup group : set) {
            result.computeIfAbsent(group.getTeacher(), teacher -> {
                int count = 0;

                for (boolean able : teacher.getDefaultAvailable()) {
                    if (able) {
                        count++;
                    }
                }

                if (count == 0) {
                    throw new IllegalStateException("Teacher " + teacher + " has zero availability");
                }

                return count;
            });
        }

        return result;
    }
}
