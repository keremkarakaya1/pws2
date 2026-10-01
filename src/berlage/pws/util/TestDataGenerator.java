package berlage.pws.util;

import berlage.pws.impl.GroupCreator;
import berlage.pws.impl.ZobristHashing;
import berlage.pws.model.*;

import java.util.*;

public class TestDataGenerator {
    private final GroupCreator groupCreator;
    private final Set<Subject> electiveSubjects;
    private final Set<Profile> profiles;
    private final Map<RoomType, Integer> countPerRoomType;
    private final Map<RoomType, List<Room>> roomMap;
    private final Map<Profile, Integer> electiveCountPerProfile;
    private final int classCapacity;
    private final int teacherCountPerSubject;
    private final int countRegular;
    private final int countGym;
    private final int countLab;
    private final int countStudents;
    private final int lessonsPerDay;
    private final int countMathTeachers;
    private final Random random;
    private final long seed;
    private static final int DAYS = 5;
    private static final Set<Subject> MATH_SUBJECTS =
            Set.copyOf(EnumSet.of(Subject.WISKUNDE_A, Subject.WISKUNDE_B, Subject.WISKUNDE_C));

    private final Map<Subject, Integer> requiredPerWeek = new EnumMap<>(Map.ofEntries(
            //TODO input from main -ignored
            Map.entry(Subject.NEDERLANDS, 4),
            Map.entry(Subject.ENGELS, 3),
            Map.entry(Subject.FRANS, 2),
            Map.entry(Subject.DUITS, 2),
            Map.entry(Subject.WISKUNDE_A, 4),
            Map.entry(Subject.WISKUNDE_B, 4),
            Map.entry(Subject.WISKUNDE_C, 3),
            Map.entry(Subject.NATUURKUNDE, 3),
            Map.entry(Subject.SCHEIKUNDE, 3),
            Map.entry(Subject.BIOLOGIE, 3),
            Map.entry(Subject.GESCHIEDENIS, 2),
            Map.entry(Subject.AARDRIJKSKUNDE, 2),
            Map.entry(Subject.ECONOMIE, 3),
            Map.entry(Subject.BEDRIJFSECONOMIE, 3),
            Map.entry(Subject.MAATSCHAPPIJLEER, 2),
            Map.entry(Subject.CKV, 1),
            Map.entry(Subject.FILOSOFIE, 2),
            Map.entry(Subject.KUNST, 2),
            Map.entry(Subject.LICHAMELIJKE_OPVOEDING, 2)
    ));



    public TestDataGenerator(Map<Profile, Integer> electiveCountPerProfile,
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
                             int lessonsPerDay) {

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
        this.seed = seed;

        roomMap = generateRooms();
        countPerRoomType = getCountPerRoomType();
        groupCreator = new GroupCreator();
        random = new Random(seed);


        validate();
    }

    public TestData build() {
        List<Room> rooms = getAllRooms();
        Map<Subject, Set<Teacher>> teacherPool = createTeachers();
        Set<Student> students = createStudents();
        Map<Subject, Set<SubjectGroup>> subjectGroups =
                groupCreator.createGroups(students, teacherPool, classCapacity);

        ZobristHashing zobristHashing = new ZobristHashing(
                lessonsPerDay, DAYS, getTeacherCount(teacherPool),
                rooms.size(), countStudents, seed + 1);

        return new TestData(
                rooms,
                teacherPool,
                countPerRoomType,
                students,
                subjectGroups,
                requiredPerWeek,
                lessonsPerDay,
                DAYS,
                zobristHashing);
    }


    private Map<RoomType, Integer> getCountPerRoomType() {
        Map<RoomType, Integer> result = new EnumMap<>(RoomType.class);

        for (RoomType type : RoomType.values()) {
            result.put(type, roomMap.get(type).size());
        }

        return result;
    }

    private List<Room> getAllRooms() {
        List<Room> result = new ArrayList<>();

        for (RoomType type : RoomType.values()) {
            result.addAll(roomMap.get(type));
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
        if (classCapacity <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: class capacity must be positive");
        }

        if (countMathTeachers <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: math teacher count must be positive");
        }

        if (Subject.values().length != requiredPerWeek.size()) {
            throw new IllegalArgumentException("INVALID INPUT: required per week map size doesn't match");
        }

        if (countStudents <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: student count must be positive");
        }

        if (lessonsPerDay <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: lessons per day must be positive");
        }

        if (teacherCountPerSubject <= 0) {
            throw new IllegalArgumentException("INVALID INPUT: teacher count per subject must be positive");
        }

        if (profiles.isEmpty()) {
            throw new IllegalArgumentException("INVALID INPUT: Empty profile set");
        }

        for (RoomType type : RoomType.values()) {
            if (roomMap.get(type).isEmpty()) {
                throw new IllegalArgumentException("INVALID INPUT: no available rooms with type: " + type);
            }
        }

        for (Profile profile : profiles) {
            if (!electiveCountPerProfile.containsKey(profile)) {
                throw new IllegalArgumentException("No elective subject count key for profile: " + profile);
            }
        }
    }

    private boolean[][] setupAvailability(boolean[] defaultAvail) {
        boolean[][] avail = new boolean[DAYS][lessonsPerDay];

        for (int i = 0; i < DAYS; i++) {
            if (defaultAvail[i]) {

                for (int j = 0; j < lessonsPerDay; j++) {
                    avail[i][j] = true;
                }
            }
        }

        return avail;
    }

    private Map<RoomType, List<Room>> generateRooms() {
        int id = 0;
        Map<RoomType, List<Room>> map = new EnumMap<>(RoomType.class);
        Map<RoomType, Integer> countPerType = Map.of(
              RoomType.REGULAR, countRegular,
              RoomType.GYM, countGym,
              RoomType.LAB, countLab
        );

        for (RoomType type : RoomType.values()) {
            map.put(type, new ArrayList<>());

            for (int i = 0; i < countPerType.get(type); i++) {
                map.get(type).add(new Room(id++, type));
            }
        }

        return map;
    }

    private boolean[] generateDefaultAvailability() {
        boolean[] arr = new boolean[DAYS];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = random.nextBoolean();
        }

        boolean zeroAvailable = true;

        for (boolean b : arr) {
            if (b) {
                zeroAvailable = false;
                break;
            }
        }

        if (zeroAvailable) {
            arr[random.nextInt(DAYS)] = true;
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
                boolean[] defaultAvailability = generateDefaultAvailability();
                boolean[][] availability = setupAvailability(defaultAvailability);

                Teacher teacher = new Teacher(
                        id++,
                        defaultAvailability,
                        availability,
                        Set.of(subject),
                        requiredType);

                teacherPool.get(subject).add(teacher);
            }
        }

        RoomType mathRoomType = roomTypeMap.get(Subject.WISKUNDE_A);

        for (int i = 0; i < countMathTeachers; i++) {
            boolean[] defaultAvailability = generateDefaultAvailability();
            boolean[][] availability = setupAvailability(defaultAvailability);

            Teacher teacher = new Teacher(
                    id++,
                    defaultAvailability,
                    availability,
                    MATH_SUBJECTS,
                    mathRoomType);

            for (Subject s : MATH_SUBJECTS) {
                teacherPool.get(s).add(teacher);
            }
        }

        return teacherPool;
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
