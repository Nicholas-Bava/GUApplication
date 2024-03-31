package guapplication.service;

import guapplication.Course;
import guapplication.Registration;
import guapplication.data.CourseRepository;
import guapplication.data.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RegisterService {

    CourseRepository courseRepository;

    RegistrationRepository registrationRepository;

    @Value("${app.semester}")
    private String currentSemester;

    @Autowired
    public RegisterService(CourseRepository courseRepository, RegistrationRepository registrationRepository){
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
    }

    public HashMap<String, Registration> registerForCourse(Registration registration, String courseName){
        HashMap<String, Registration> response = new HashMap<>();

        Registration newRegistration = createNewRegistration(registration);
        // Check if course exists
        if (courseRepository.findAllByName(courseName).isEmpty()){
            response.put("Course Does Not Exist", null);
            return response;
        }

        // Check if course is available this semester
        if (courseRepository.findCourseByNameAndSemester(courseName, currentSemester) == null){
            response.put("Course Not Found for Current Semester", null);
            return response;
        }

        // Check if the course has available slots
        if (!courseRepository.findCourseByNameAndSemester(courseName, currentSemester).isOpen()){
            response.put("The Course Is Full", null);
            return response;
        }

        Course course = courseRepository.findCourseByNameAndSemester(courseName, currentSemester);

        // Check if applicant is already registered for the course
        if (course.getRegistrations().stream().anyMatch(reg -> reg.getEmail().equals(newRegistration.getEmail()))){
            response.put("You have already registered for this course", null);
            return response;
        }

        // Early bird gets the worm
        if (course.getRegistrations().isEmpty()){
            newRegistration.setStatus(Registration.Status.ACCEPTED);
        }

        // Check both if registration is STEM Underrepresented AND if there are currently none registered in the course
        if (newRegistration.isUnderRepresented() && course.getRegistrations().stream().noneMatch(reg -> reg.isUnderRepresented())){
            newRegistration.setStatus(Registration.Status.ACCEPTED);
        }

        // Register for course, close and run acceptance algo if # enrolled is twice the capacity
        newRegistration.setCourse(course);
        course.getRegistrations().add(newRegistration);

        registrationRepository.save(newRegistration);
        courseRepository.save(course);

        if (course.getRegistrations().size() >= course.getCapacity() * 2){
            course.setOpen(false);
            acceptanceAlgorithm(course);
        }

        response.put("Registered Successfully!", newRegistration);
        return response;
    }

    private void acceptanceAlgorithm(Course course){

        // First check if any have already been accepted based on priority acceptance checks
        int remainingSlots = course.getCapacity() - course.getRegistrations().stream().filter(reg -> reg.getStatus() ==
                Registration.Status.ACCEPTED).toList().size();



        // Loop through available slots, assigning one person until full
        for (int i = 0; i < remainingSlots; i++){
            Registration registrationToChoose;

            // Find males, females, and all who are in pending status
            List<Registration> pendingMales = registrationRepository.findAllByGenderAndStatusOrderByCreateDateAsc("male",
                    Registration.Status.PENDING).stream().filter(reg -> reg.getCourse().getName().equals(course.getName())).toList();

            List<Registration> pendingFemales = registrationRepository.findAllByGenderAndStatusOrderByCreateDateAsc("female",
                    Registration.Status.PENDING).stream().filter(reg -> reg.getCourse().getName().equals(course.getName())).toList();

            List<Registration> pendingAll = registrationRepository.findAllByStatusOrderByCreateDateAsc(Registration.Status.PENDING).stream().filter(reg -> reg.getCourse().getName().equals(course.getName())).toList();

            // Find currently accepted males and females to compare current numbers
            List<Registration> acceptedMales = registrationRepository.findAllByGenderAndStatusOrderByCreateDateAsc("male",
                    Registration.Status.ACCEPTED).stream().filter(reg -> reg.getCourse().getName().equals(course.getName())).toList();

            List<Registration> acceptedFemales = registrationRepository.findAllByGenderAndStatusOrderByCreateDateAsc("female",
                    Registration.Status.ACCEPTED).stream().filter(reg -> reg.getCourse().getName().equals(course.getName())).toList();


            // If equal number of males and females, go by created date
            if (acceptedMales.size() == acceptedFemales.size()){

                registrationToChoose = pendingAll.get(0);

                // If more males than females, prioritize female if available, otherwise use created date
            } else if (acceptedMales.size() > acceptedFemales.size()){

                if (!pendingFemales.isEmpty()){
                    registrationToChoose = pendingFemales.get(0);
                } else {
                    registrationToChoose = pendingMales.get(0);
                }

                // If more females than males, prioritize male if available, otherwise use created date
            } else {
                if (!pendingMales.isEmpty()){
                    registrationToChoose = pendingMales.get(0);
                } else {
                    registrationToChoose = pendingFemales.get(0);
                }
            }

            // Save registration and course
            registrationToChoose.setStatus(Registration.Status.ACCEPTED);
            registrationRepository.save(registrationToChoose);
            courseRepository.save(course);
        }

        // Decline remaining pending registrations
        List<Registration> pendingToDecline = registrationRepository.findAllByStatusOrderByCreateDateAsc(Registration.Status.PENDING).stream().filter(reg -> reg.getCourse().getName().equals(course.getName())).toList();

        for (int i = 0; i < pendingToDecline.size(); i++){
            Registration registrationToDecline = pendingToDecline.get(i);
            registrationToDecline.setStatus(Registration.Status.DECLINED);
            registrationRepository.save(registrationToDecline);
            courseRepository.save(course);
        }
    }

    public List<Registration> findByUuid (UUID uuid){
        return registrationRepository.findAllByUuid(uuid);
    }

    public List<Registration> findByEmail(String email){
        return registrationRepository.findAllByEmail(email);
    }

    public Registration createNewRegistration(Registration registration){
        Registration regToSave = new Registration();
        regToSave.setName(registration.getName());
        regToSave.setEmail(registration.getEmail());
        regToSave.setDateOfBirth(registration.getDateOfBirth());
        regToSave.setRace(registration.getRace());
        regToSave.setGender(registration.getGender());
        regToSave.setUnderRepresented(registration.isUnderRepresented());
        return regToSave;
    }

}
