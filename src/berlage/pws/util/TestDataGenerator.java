package berlage.pws.util;

import berlage.pws.impl.GroupCreator;
import berlage.pws.impl.LessonInstanceBuilder;
import berlage.pws.model.*;

import java.util.*;

public class TestDataGenerator {
    private final Map<Subject, Integer> requiredPerWeek;
    private final GroupCreator groupCreator;
    private final Set<Subject> electiveSubjects;
    private final Set<Profile> profiles;
    private final Map<RoomType, Integer> countPerRoomType;
    private final Map<RoomType, Room[]> roomMap;
    private final Map<Profile, Integer> electiveCountPerProfile;
    private final int classCapacity;
    private final int teacherCountPerSubject;
    private final int countRegular;
    private final int countGym;
    private final int countLab;
    private final int countStudents;
    private final int lessonsPerDay;
    private final int countMathTeachers;
    private final int maxCountSubjectPerDay;
    private final Random random;
    private final long seed;
    private final int days;
    private static final Set<Subject> MATH_SUBJECTS =
            Set.copyOf(EnumSet.of(Subject.WISKUNDE_A, Subject.WISKUNDE_B, Subject.WISKUNDE_C));

    public TestDataGenerator(Map<Subject, Integer> requiredPerWeek,
                             Map<Profile, Integer> electiveCountPerProfile,
                             Set<Profile> profiles,
                             Set<Subject> electiveSubjects,
                             long seed,
                             int classCapacity,
                             int teacherCountPerSubject,
                             int countRegular,
                             int countGym,
                             int countLab,
                             int countMathTeachers,
                             int countStudents,
                             int lessonsPerDay,
                             int maxCountSubjectPerDay,
                             int days) {

        this.requiredPerWeek = requiredPerWeek;
        this.electiveSubjects = electiveSubjects;
        this.profiles = profiles;
        this.electiveCountPerProfile = electiveCountPerProfile;
        this.classCapacity = classCapacity;
        this.teacherCountPerSubject = teacherCountPerSubject;
        this.countRegular = countRegular;
        this.countGym = countGym;
        this.countLab = countLab;
        this.countStudents = countStudents;
        this.countMathTeachers = countMathTeachers;
        this.lessonsPerDay = lessonsPerDay;
        this.maxCountSubjectPerDay = maxCountSubjectPerDay;
        this.days = days;
        this.seed = seed;

        roomMap = generateRooms();
        countPerRoomType = getCountPerRoomType();
        groupCreator = new GroupCreator();
        random = new Random(seed);


        validate();
    }

    public TestData build() {
        Slot[] slots = createSlots();
        List<Room> rooms = getAllRooms();
        Map<Subject, Set<Teacher>> teacherPool = createTeachers();
        Set<Student> students = createStudents();
        Map<Subject, Set<SubjectGroup>> subjectGroups =
                groupCreator.createGroups(students, teacherPool, requiredPerWeek, lessonsPerDay, classCapacity);

        int teacherCount = getTeacherCount(teacherPool);
        int groupCount = 0;

        for (Set<SubjectGroup> set : subjectGroups.values()) {
            groupCount += set.size();
        }

        Map<Teacher, int[]> studentsOfTeacherMap = getConvertedTeacherStudentIdMap(getTeacherStudentIdMap(subjectGroups));
        List<SubjectGroup> lessonInstances = LessonInstanceBuilder.build(countPerRoomType, subjectGroups, requiredPerWeek);
        List<List<SubjectGroup>> subjectGroupsPerStudentList = getSubjectGroupsPerStudent(students.size(), lessonInstances);
        SubjectGroup[][] subjectGroupsPerStudent = getArraySubjectGroupsPerStudent(subjectGroupsPerStudentList);
        int[][] countGroupsToBeScheduledAtIndex = getCountGroupsToBeScheduled(groupCount, lessonInstances);
        int[][] lessonsLeft = createLessonCountTable(lessonInstances, students);

        return new TestData(
                roomMap,
                teacherPool,
                countPerRoomType,
                students,
                subjectGroups,
                requiredPerWeek,
                studentsOfTeacherMap,
                lessonInstances,
                subjectGroupsPerStudent,
                slots,
                countGroupsToBeScheduledAtIndex,
                lessonsLeft,
                groupCount,
                rooms.size(),
                teacherCount,
                lessonsPerDay,
                days,
                maxCountSubjectPerDay);
    }


    private Map<Teacher, List<Integer>> getTeacherStudentIdMap(Map<Subject, Set<SubjectGroup>> subjectGroups) {
        Map<Teacher, List<Integer>> teacherStudentIdMap = new HashMap<>();

        for (Set<SubjectGroup> groups : subjectGroups.values()) {
            for (SubjectGroup group : groups) {
                Teacher teacher = group.getTeacher();
                teacherStudentIdMap.putIfAbsent(teacher, new ArrayList<>());

                for (int student : group.getStudents()) {
                    teacherStudentIdMap.get(teacher).add(student);
                }
            }
        }

        return teacherStudentIdMap;
    }

    private Map<Teacher, int[]> getConvertedTeacherStudentIdMap(Map<Teacher, List<Integer>> map) {
        Map<Teacher, int[]> result = new HashMap<>();

        for (Map.Entry<Teacher, List<Integer>> entry : map.entrySet()) {
            List<Integer> list = entry.getValue();
            result.put(entry.getKey(), new int[list.size()]);
            int[] arr = result.get(entry.getKey());

            for (int i = 0; i < arr.length; i++) {
                arr[i] = list.get(i);
            }
        }

        return result;
    }

    private Map<RoomType, Integer> getCountPerRoomType() {
        Map<RoomType, Integer> result = new EnumMap<>(RoomType.class);

        for (RoomType type : RoomType.values()) {
            result.put(type, roomMap.get(type).length);
        }

        return result;
    }

    private List<Room> getAllRooms() {
        List<Room> result = new ArrayList<>();

        for (RoomType type : RoomType.values()) {
            result.addAll(List.of(roomMap.get(type)));
        }

        return result;
    }

    private int[][] createLessonCountTable(List<SubjectGroup> lessonInstances, Set<Student> students) {
        int[][] result = new int[students.size()][lessonInstances.size() + 1];

        for (int i = lessonInstances.size() - 1; i >= 0; i--) {
            SubjectGroup group = lessonInstances.get(i);

            for (Student student : students) {
                result[student.getId()][i] = result[student.getId()][i + 1];
            }

            for (Student student : group.getStudentsSet()) {
                result[student.getId()][i]++;
            }
        }

        return result;
    }


    private int[][] getCountGroupsToBeScheduled(int groupCount, List<SubjectGroup> lessonInstances) {
        int[][] result = new int[groupCount][lessonInstances.size() + 1];

        for (int j = result[0].length - 2; j >= 0; j--) {

            for (int k = 0; k < result.length; k++) {
                result[k][j] = result[k][j+1];
            }

            SubjectGroup group = lessonInstances.get(j);

            result[group.getId()][j]++;
        }

        return result;
    }

    private int getTeacherCount(Map<Subject, Set<Teacher>> teacherPool) {
        Set<Teacher> distinctSet = new HashSet<>();


        for (Set<Teacher> teacherSet : teacherPool.values()) {
            distinctSet.addAll(teacherSet);
        }

        return distinctSet.size();
    }

    private void validate() {
        if (days <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: day count must be positive");
        }

        if (classCapacity <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: class capacity must be positive");
        }

        if (countMathTeachers <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: math teacher count must be positive");
        }

        if (Subject.values().length != requiredPerWeek.size()) {
            throw new IllegalArgumentException("INVALID INPUT: required per week map size doesn't match");
        }

        if (maxCountSubjectPerDay <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: divide by 0 -- maxCountSubjectPerDay");
        }

        if (countStudents <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: student count must be positive");
        }

        if (lessonsPerDay <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: lessons per day must be positive");
        }

        if (lessonsPerDay > 31) {
            throw new IllegalArgumentException("INVALID INPUT: lessons per day cannot be more than 31");
        }

        if (teacherCountPerSubject <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: teacher count per subject must be positive");
        }

        if (profiles.isEmpty()) {
            throw new IllegalArgumentException("INVALID INPUT: Empty profile set");
        }

        int max = Collections.max(requiredPerWeek.values());

        if (max > maxCountSubjectPerDay * days) {
            throw new IllegalArgumentException("INVALID INPUT: lessons cannot fit in a week -- increase days or lower requiredPerWeek");
        }

        for (RoomType type : RoomType.values()) {
            if (roomMap.get(type).length == 0) {
                throw new IllegalArgumentException("INVALID INPUT: no available rooms with type: " + type);
            }
        }

        for (Profile profile : profiles) {
            if (!electiveCountPerProfile.containsKey(profile)) {
                throw new IllegalArgumentException("No elective subject count key for profile: " + profile);
            }
        }
    }


    private List<List<SubjectGroup>> getSubjectGroupsPerStudent(int studentCount, List<SubjectGroup> lessonInstances) {
        List<List<SubjectGroup>> result = new ArrayList<>();

        for (int i = 0; i < studentCount; i++) {
            result.add(new ArrayList<>());
        }

        Set<SubjectGroup> distinct = new HashSet<>();

        for (int i = 0; i < lessonInstances.size(); i++) {
            if (distinct.add(lessonInstances.get(i))) {
                SubjectGroup group = lessonInstances.get(i);

                for (Student student : group.getStudentsSet()) {
                    result.get(student.getId()).add(group);
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    private SubjectGroup[][] getArraySubjectGroupsPerStudent(List<List<SubjectGroup>> list) {
        SubjectGroup[][] result = new SubjectGroup[list.size()][];

        for (int i = 0; i < list.size(); i++) {
            List<SubjectGroup> inner = list.get(i);
            result[i] = new SubjectGroup[inner.size()];

            for (int j = 0; j < inner.size(); j++) {
                result[i][j] = inner.get(j);
            }
        }

        return result;
    }

    private Map<RoomType, Room[]> generateRooms() {
        int id = 0;
        Map<RoomType, Room[]> map = new EnumMap<>(RoomType.class);
        Map<RoomType, Integer> countPerType = Map.of(
              RoomType.REGULAR, countRegular,
              RoomType.GYM, countGym,
              RoomType.LAB, countLab
        );

        for (RoomType type : RoomType.values()) {
            map.put(type, new Room[countPerType.get(type)]);

            for (int i = 0; i < countPerType.get(type); i++) {
                map.get(type)[i] = new Room(id++, type);
            }
        }

        return map;
    }

    private int[] generateDefaultAvailability(Set<Subject> subjects) {
        int[] arr = new int[days];

        for (int i = 0; i < arr.length; i++) {
            boolean val = random.nextBoolean();

            if (val) {
                int res = 0;

                for (int j = 0; j < lessonsPerDay; j++) {
                    res |= (1 << j);
                }

                arr[i] = res;
            }
        }

        //Arrays.fill(arr, true);

        int count = 0;
        int missing = 0;

        for (int num : arr) {
            if (num > 0) {
                count++;
            }
        }

        for (Subject subject : subjects) {
            int required = requiredPerWeek.get(subject);

            if (count < Math.ceil((double) required / maxCountSubjectPerDay)) {
                missing = (int) Math.max(missing, Math.ceil((double) required / maxCountSubjectPerDay) - count);
            }
        }

        if (missing == 0) {
            return arr;
        }

        List<Integer> freeIndexes = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                freeIndexes.add(i);
            }
        }

        if (missing > freeIndexes.size()) {
            throw new IllegalArgumentException("TEACHER AVAILABILITY GENERATOR FAIL: subject can't fit in " + days + " days at this cap");
        }

        while (missing > 0) {
            int selectedIndex = random.nextInt(freeIndexes.size());
            int res = 0;

            for (int j = 0; j < lessonsPerDay; j++) {
                res |= (1 << j);
            }

            arr[freeIndexes.get(selectedIndex)] = res;
            freeIndexes.remove(selectedIndex);

            missing--;
        }

        return arr;
    }

    private Map<Subject, RoomType> getTypesPerSubject() {
        Map<Subject, RoomType> roomTypes = new EnumMap<>(Subject.class);

        for (Subject s : Subject.values()) {
            roomTypes.put(s, RoomType.REGULAR);
        }

        roomTypes.put(Subject.NATUURKUNDE, RoomType.LAB);
        roomTypes.put(Subject.SCHEIKUNDE, RoomType.LAB);
        roomTypes.put(Subject.LICHAMELIJKE_OPVOEDING, RoomType.GYM);

        return roomTypes;
    }

    private Map<Subject, Set<Teacher>> createTeachers() {
        Map<Subject, Set<Teacher>> teacherPool = new EnumMap<>(Subject.class);
        Map<Subject, RoomType> roomTypeMap = getTypesPerSubject();
        int id = 0;

        for (Subject subject : Subject.values()) {
            teacherPool.put(subject, new HashSet<>());

            if (MATH_SUBJECTS.contains(subject)) {
                continue;
            }

            RoomType requiredType = roomTypeMap.get(subject);

            for (int i = 0; i < teacherCountPerSubject; i++) {
                int[] defaultAvailability = generateDefaultAvailability(Set.of(subject));

                Teacher teacher = new Teacher(
                        id++,
                        defaultAvailability,
                        Set.of(subject),
                        requiredType);

                teacherPool.get(subject).add(teacher);
            }
        }

        RoomType mathRoomType = roomTypeMap.get(Subject.WISKUNDE_A);

        for (int i = 0; i < countMathTeachers; i++) {
            int[] defaultAvailability = generateDefaultAvailability(MATH_SUBJECTS);

            Teacher teacher = new Teacher(
                    id++,
                    defaultAvailability,
                    MATH_SUBJECTS,
                    mathRoomType);

            for (Subject s : MATH_SUBJECTS) {
                teacherPool.get(s).add(teacher);
            }
        }

        return teacherPool;
    }

    private Slot[] createSlots() {
        Slot[] slots = new Slot[days * lessonsPerDay];

        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Slot(i / lessonsPerDay, i % lessonsPerDay, i);
        }

        return slots;
    }

    private Set<Student> createStudents() {
        Set<Student> students = new HashSet<>();
        List<Profile> list = new ArrayList<>(profiles);
        list.sort(Comparator.comparingInt(Profile::getId));
        int index = 0;
        int id = 0;

        for (int i = 0; i < countStudents; i++) {
            Profile profile = list.get(index++ % profiles.size());
            students.add(new Student(profile, getElectiveSubjects(profile), id++));
        }

        return students;
    }

    private Set<Subject> getElectiveSubjects(Profile profile) {
        Set<Subject> result = new HashSet<>();
        Set<Subject> mandatory = profile.getMandatorySubjects();
        List<Subject> possibleElectives = new ArrayList<>();

        for (Subject subject : electiveSubjects) {
            if (!mandatory.contains(subject) && !MATH_SUBJECTS.contains(subject)) {
                possibleElectives.add(subject);
            }
        }

        Collections.shuffle(possibleElectives, random);
        int count = electiveCountPerProfile.get(profile);

        if (count > possibleElectives.size()) {
            System.err.println("elective count for profile: " + profile + " is bigger than possible elective subject count");
        }

        for (int i = 0; i < Math.min(count, possibleElectives.size()); i++) {
            result.add(possibleElectives.get(i));
        }

        return result;
    }
}
