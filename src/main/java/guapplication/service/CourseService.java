package guapplication.service;

import guapplication.Course;
import guapplication.Registration;
import guapplication.data.CourseRepository;
import guapplication.data.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    CourseRepository courseRepository;

    RegistrationRepository registrationRepository;

    @Value("${app.semester}")
    private String currentSemester;

    @Autowired
    public CourseService(CourseRepository courseRepository, RegistrationRepository registrationRepository){
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
    }
    public List<Course> getAllCourses(){
        return courseRepository.findAllBySemester(currentSemester);
    }

    public List<Registration> getAllApplicantsOfCourse(Long courseID){
        return courseRepository.findCourseByCourseID(courseID).getRegistrations();
    }

}
