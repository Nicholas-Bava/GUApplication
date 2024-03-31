package guapplication.data;

import guapplication.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Course findCourseByNameAndSemester(String name, String semester);

    List<Course> findAllBySemester(String semester);

    List<Course> findAllByName(String name);

    Course findCourseByCourseID(Long courseID);
}