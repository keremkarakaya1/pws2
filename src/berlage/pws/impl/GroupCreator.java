package berlage.pws.impl;

import berlage.pws.model.*;

import java.util.*;

public class GroupCreator {

    private static final Comparator<Student> STUDENT_CLUSTER_COMPARATOR = Comparator
            .comparingInt((Student s) -> s.getProfile().getSecondaryId())
            .thenComparingInt(s -> s.getProfile().getId())
            .thenComparing(s -> {
                List<Subject> sorted = new ArrayList<>(s.getElectiveSubjects());
                sorted.sort(Comparator.naturalOrder());
                return sorted.toString();
            })
            .thenComparingInt(Student::getId);

    public Map<Subject, Set<SubjectGroup>> createGroups(Set<Student> students, Map<Subject, Set<Teacher>> teacherPool , int classCapacity) {
        Map<Subject, Set<SubjectGroup>> result = new EnumMap<>(Subject.class);
        Map<Subject, Set<Student>> demandMap = new EnumMap<>(Subject.class);
        Map<Subject, Integer> countPerSubject = new EnumMap<>(Subject.class);
        Map<Subject, List<Set<Student>>> groupsPerSubject = new EnumMap<>(Subject.class);

        for (Student student : students) {
            Profile profile = student.getProfile();
            Set<Subject> electiveSet = student.getElectiveSubjects();
            Set<Subject> mandatorySubjects = profile.getMandatorySubjects();

            for (Subject subject : mandatorySubjects) {
                demandMap.computeIfAbsent(subject, k -> new HashSet<>()).add(student);
            }

            for (Subject subject : electiveSet) {
                demandMap.computeIfAbsent(subject, k -> new HashSet<>()).add(student);
            }
        }

        for (Map.Entry<Subject, Set<Student>> entry : demandMap.entrySet()) {
            Set<Student> set = entry.getValue();
            Subject subject = entry.getKey();
            countPerSubject.put(subject, (int) Math.ceil((double)set.size() / classCapacity));
        }

        for (Map.Entry<Subject, Set<Student>> entry : demandMap.entrySet()) {
            Subject subject = entry.getKey();
            List<Student> studentList = new ArrayList<>(entry.getValue());
            studentList.sort(STUDENT_CLUSTER_COMPARATOR);

            int numGroups = countPerSubject.get(subject);
            int baseSize = studentList.size() / numGroups;
            int remainder = studentList.size() % numGroups;

            List<Set<Student>> chunks = new ArrayList<>();
            int index = 0;

            for (int i = 0; i < numGroups; i++) {
                int chunkSize = baseSize + (i < remainder ? 1 : 0);
                Set<Student> chunk = new HashSet<>(studentList.subList(index, index + chunkSize));
                chunks.add(chunk);
                index += chunkSize;
            }

            groupsPerSubject.put(subject, chunks);
        }

        int id = 0;

        for (Map.Entry<Subject, List<Set<Student>>> entry : groupsPerSubject.entrySet()) {
            Subject subject = entry.getKey();
            List<Set<Student>> groups = entry.getValue();
            List<Teacher> teachers = new ArrayList<>(teacherPool.get(subject));
            teachers.sort(Comparator.comparingInt(Teacher::getId));

            Set<SubjectGroup> subjectGroups = new HashSet<>();
            int counter = 0;

            for (Set<Student> group : groups) {
                Teacher teacher = teachers.get(counter % teachers.size());
                subjectGroups.add(new SubjectGroup(teacher, subject, group, id++));
                counter++;
            }

            result.put(subject, subjectGroups);
        }

        return result;
    }
}
