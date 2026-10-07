package berlage.pws.util;

import berlage.pws.model.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class OutputManager {

    private final int lessonsPerDay;
    private final int days;
    private final String outputPath;

    public OutputManager(int lessonsPerDay, int days, String outputPath) {
        this.lessonsPerDay = lessonsPerDay;
        this.days = days;
        this.outputPath = outputPath;
    }

    public void outputSchedule(ScheduleState state) {
        ScheduleState movingPointer = state;
        Map<Student, List<ScheduledLesson>> lessonsPerStudent = new HashMap<>();

        while (movingPointer != null && movingPointer.getPlacedLesson() != null) {
            ScheduledLesson lesson = movingPointer.getPlacedLesson();
            SubjectGroup group = lesson.getGroup();
            Set<Student> students = group.getStudents();

            for (Student student : students) {
                lessonsPerStudent.computeIfAbsent(student, k -> new ArrayList<>()).add(lesson);
            }

            movingPointer = movingPointer.getParent();
        }

        ScheduledLesson[][][] schedules = new ScheduledLesson[lessonsPerStudent.size()][days][lessonsPerDay];

        for (Map.Entry<Student, List<ScheduledLesson>> entry : lessonsPerStudent.entrySet()) {
            Student student = entry.getKey();
            List<ScheduledLesson> lessons = entry.getValue();

            for (ScheduledLesson lesson : lessons) {
                Slot slot = lesson.getSlot();
                int day = slot.getDay();
                int period = slot.getPeriod();

                schedules[student.getId()][day][period] = lesson;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");

        for (int i = 0; i < schedules.length; i++) {
            ScheduledLesson[][] innerSchedule = schedules[i];
            sb.append("<h2>").append(i).append("</h2>");
            sb.append("<table style=\"border-collapse: collapse;\">");

            for (int j = 0; j < innerSchedule.length; j++) {
                sb.append("<tr>");

                for (int k = 0; k < innerSchedule[0].length; k++) {
                    sb.append("<td style=\"border: 1px solid black; padding: 5px;\">");

                    if (innerSchedule[j][k] == null) {
                        sb.append("EMPTY");
                    } else {
                        sb.append(innerSchedule[j][k]);
                    }

                    sb.append("</td>");
                }

                sb.append("</tr>");
            }

            sb.append("</table>");
        }

        sb.append("</body></html>");

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath))) {
            bufferedWriter.write(sb.toString());
        } catch (IOException exc) {
            System.out.println("exception");
            throw new RuntimeException(exc);
        }
    }
}
