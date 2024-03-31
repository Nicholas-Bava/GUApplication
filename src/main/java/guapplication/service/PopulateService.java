package guapplication.service;

import guapplication.Course;
import guapplication.data.CourseRepository;
import guapplication.data.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PopulateService {

    CourseRepository courseRepository;

    RegistrationRepository registrationRepository;

    @Autowired
    public PopulateService(CourseRepository courseRepository, RegistrationRepository registrationRepository){
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
    }

    public void populate() {
        Course course1 = createCourse("CALCULUS 2112", "Calculus II", true, "Summer 2024");
        Course course2 = createCourse("HISTORY 1112", "World History", true, "Summer 2024");
        Course course3 = createCourse("ENGLISH 1421", "British Literature", true, "Summer 2024");
        Course course4 = createCourse("CALCULUS 2113", "Calculus III", false, "Fall 2024");

        courseRepository.save(course1);
        courseRepository.save(course2);
        courseRepository.save(course3);
        courseRepository.save(course4);
    }

    private Course createCourse(String name, String description, boolean open, String semester){
        Course newCourse = new Course();
        newCourse.setName(name);
        newCourse.setDescription(description);
        newCourse.setOpen(open);
        newCourse.setSemester(semester);
        return newCourse;
    }

}
