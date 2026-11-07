package berlage.pws.util;

import berlage.pws.model.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class FileWriter {
    public void writeTestData(TestData data) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new java.io.FileWriter("testDataFile.txt"))) {
            for (Map.Entry<Subject, Integer> entry : data.requiredPerWeek().entrySet()) {
                bufferedWriter.write("Subject: " + entry.getKey() + " -> requiredPerWeek: " + entry.getValue());
                bufferedWriter.newLine();
            }

            for (Student student : data.students()) {
                bufferedWriter.write("Student Info: " + student + " Electives: " + student.getElectiveSubjects());
                bufferedWriter.newLine();
            }

            bufferedWriter.newLine();

            for (Map.Entry<Subject, Set<Teacher>> entry : data.teacherPool().entrySet()) {
                bufferedWriter.write("Subject: " + entry.getKey() + " Teachers: " + entry.getValue());
                bufferedWriter.newLine();
            }

            Set<Teacher> all = new HashSet<>();

            for (Set<Teacher> set : data.teacherPool().values()) {
                all.addAll(set);
            }

            for (Teacher teacher : all) {
                bufferedWriter.write("Teacher: " + teacher + " availability: " + Arrays.toString(teacher.getDefaultAvailable()));
                bufferedWriter.newLine();
            }

            bufferedWriter.newLine();

            for (Map.Entry<RoomType, Room[]> entry : data.roomMap().entrySet()) {
                bufferedWriter.write("RoomType: " + entry.getKey() + " Rooms: " + Arrays.toString(entry.getValue()));
                bufferedWriter.newLine();
            }

            bufferedWriter.newLine();

            for (Map.Entry<Subject, Set<SubjectGroup>> entry : data.subjectGroups().entrySet()) {
                bufferedWriter.write("Subject: " +  entry.getKey() + " Groups: " + entry.getValue());
                bufferedWriter.newLine();
            }

            bufferedWriter.newLine();

        } catch (IOException exc) {
            System.out.println("EXC");

        }
    }
}
