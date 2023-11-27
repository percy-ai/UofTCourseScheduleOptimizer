package Distance;

import API.CourseAPI;
import entity.Course;

import java.util.List;
import java.util.ArrayList;
import java.util.List;

/** Demo of creating two courses through the CourseAPI and displaying all distance info between their buildings.
 * @author Joshua Jang
 */
public class DistanceDemo {
    public static void main(String[] args) {
        List<Course> courses = new ArrayList<>();

        Course csc207 = new Course(CourseAPI.getCourse("CSC207H1 -F"));
        Course cog250 = new Course(CourseAPI.getCourse("MAT235Y1 -Y"));

        courses.add(csc207);
        courses.add(cog250);

        // Generate cache of all possible distance combinations. MUST DO prior to using getDistanceFloat.
        DistanceManager.updateDistances(courses);
        DistanceData distanceData = DistanceManager.getDistanceData("BA", "FE");
        System.out.println(distanceData.getDistanceFloat());
    }
}
