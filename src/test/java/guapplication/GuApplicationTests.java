package guapplication;

import guapplication.data.CourseRepository;
import guapplication.data.RegistrationRepository;
import guapplication.service.CourseService;
import guapplication.service.PopulateService;
import guapplication.service.RegisterService;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GuApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	CourseRepository testCourseRepository;

	@Autowired
	RegistrationRepository testRegistrationRepository;

	@Autowired
	RegisterService testRegisterService;

	@Autowired
	PopulateService populateService;

	@Autowired
	CourseService courseService;

	String currentSemester = "Summer 2024";

	@BeforeAll
	public void startUp(){
		System.out.println("starting up");
		populateService.populate();
	}

	@Transactional
	@Test
	public void testFirstRegistration(){
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");

		String testCourseName = "CALCULUS 2112";

		HashMap<String,Registration> testResult = testRegisterService.registerForCourse(testReg, testCourseName);

		HashMap.Entry<String, Registration> entry = testResult.entrySet().iterator().next();

		assertEquals( "Registered Successfully!", entry.getKey());

		assertEquals(Registration.Status.ACCEPTED, entry.getValue().getStatus());

	}

	@Transactional
	@Test
	public void testAlreadyRegistered(){
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");
		Registration testReg2 = createRegistration("nick", "nick@baylor.edu", false, "male");

		String testCourseName = "CALCULUS 2112";

		testRegisterService.registerForCourse(testReg, testCourseName);

		HashMap<String,Registration> testResult = testRegisterService.registerForCourse(testReg2, testCourseName);

		HashMap.Entry<String, Registration> entry = testResult.entrySet().iterator().next();

		assertEquals( "You have already registered for this course", entry.getKey());

	}

	@Transactional
	@Test
	public void testWrongSemester(){
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");

		String testCourseName = "CALCULUS 2113";

		HashMap<String,Registration> testResult = testRegisterService.registerForCourse(testReg, testCourseName);

		HashMap.Entry<String, Registration> entry = testResult.entrySet().iterator().next();

		assertEquals( "Course Not Found for Current Semester", entry.getKey());

	}

	@Transactional
	@Test
	public void testWrongClassName(){
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");

		String testCourseName = "CALCULUS 3200";

		HashMap<String,Registration> testResult = testRegisterService.registerForCourse(testReg, testCourseName);

		HashMap.Entry<String, Registration> entry = testResult.entrySet().iterator().next();

		assertEquals( "Course Does Not Exist", entry.getKey());

	}

	@Transactional
	@Test
	public void testUnderRepresented(){
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");
		Registration testReg2 = createRegistration("katie", "katie@baylor.edu", true, "female");

		String testCourseName = "CALCULUS 2112";

		testRegisterService.registerForCourse(testReg, testCourseName);

		HashMap<String,Registration> testResult = testRegisterService.registerForCourse(testReg2, testCourseName);

		HashMap.Entry<String, Registration> entry = testResult.entrySet().iterator().next();

		assertEquals( Registration.Status.ACCEPTED, entry.getValue().getStatus());

	}

	@Transactional
	@Test
	public void testAcceptanceAlgorithm(){
		// Test should result in first 2 males and first 2 females being accepted
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");
		Registration testReg2 = createRegistration("jack", "jack@baylor.edu", false, "male");
		Registration testReg3 = createRegistration("john", "john@baylor.edu", false, "male");
		Registration testReg4 = createRegistration("paul", "paul@baylor.edu", false, "male");
		Registration testReg5 = createRegistration("katie", "katie@baylor.edu", false, "female");
		Registration testReg6 = createRegistration("suzie", "suzie@baylor.edu", false, "female");
		Registration testReg7 = createRegistration("sally", "sally@baylor.edu", false, "female");
		Registration testReg8 = createRegistration("amber", "amber@baylor.edu", false, "female");

		String testCourseName = "CALCULUS 2112";

		testRegisterService.registerForCourse(testReg, testCourseName);
		testRegisterService.registerForCourse(testReg2, testCourseName);
		testRegisterService.registerForCourse(testReg3, testCourseName);
		testRegisterService.registerForCourse(testReg4, testCourseName);
		testRegisterService.registerForCourse(testReg5, testCourseName);
		testRegisterService.registerForCourse(testReg6, testCourseName);
		testRegisterService.registerForCourse(testReg7, testCourseName);
		testRegisterService.registerForCourse(testReg8, testCourseName);

		List<Registration> testResult = courseService.getAllApplicantsOfCourse(1L);

		List<String> acceptedEmails = new ArrayList<>();

		for (int i = 0; i < testResult.size(); i++){
			if (testResult.get(i).getStatus() == Registration.Status.ACCEPTED){
				acceptedEmails.add(testResult.get(i).getEmail());
			}
		}

		assertEquals(4, acceptedEmails.size());
		assertTrue(acceptedEmails.contains("nick@baylor.edu") &&
				acceptedEmails.contains("jack@baylor.edu") &&
				acceptedEmails.contains("katie@baylor.edu") &&
				acceptedEmails.contains("suzie@baylor.edu"));

	}

	@Test
	@Transactional
	public void testGetActiveCourses(){
		List<Course> activeCourses = courseService.getAllCourses();
		List<String> selectedSemesters = new ArrayList<>();

		for (Course course: activeCourses){
			selectedSemesters.add(course.getSemester());
		}

		assertTrue(selectedSemesters.contains(currentSemester));
		assertFalse(selectedSemesters.contains("Fall 2024"));
	}

	@Transactional
	@Test
	public void testGetRegistered(){
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");
		Registration testReg2 = createRegistration("katie", "katie@baylor.edu", false, "female");

		String testCourseName = "CALCULUS 2112";

		testRegisterService.registerForCourse(testReg, testCourseName);
		testRegisterService.registerForCourse(testReg2, testCourseName);

		List<Registration> registered = courseService.getAllApplicantsOfCourse(1L);

		List<String> registeredEmails = new ArrayList<>();

		for (Registration reg: registered){
			registeredEmails.add(reg.getEmail());
		}

		assertEquals(2, registered.size());
		assertTrue(registeredEmails.contains("nick@baylor.edu") && registeredEmails.contains("katie@baylor.edu"));

	}

	@Transactional
	@Test
	public void testGetRegisteredForPerson(){
		Registration testReg = createRegistration("nick", "nick@baylor.edu", false, "male");
		Registration testReg2 = createRegistration("nick", "nick@baylor.edu", false, "male");

		String testCourseName = "CALCULUS 2112";
		String testCourseName2 = "HISTORY 1112";

		testRegisterService.registerForCourse(testReg, testCourseName);
		testRegisterService.registerForCourse(testReg2, testCourseName2);

		List<Registration> regList = testRegisterService.findByEmail(testReg.getEmail());

		List<String> courseNames = new ArrayList<>();

		for (Registration reg: regList){
			courseNames.add(reg.getCourse().getName());
		}

		assertEquals(2, regList.size());
		assertTrue(courseNames.contains("CALCULUS 2112") && courseNames.contains("HISTORY 1112"));

	}



	public Registration createRegistration(String name, String email, boolean underRepresented, String gender){
		Registration reg = new Registration();
		reg.setName(name);
		reg.setEmail(email);
		reg.setUnderRepresented(underRepresented);
		reg.setGender(gender);

		return reg;
	}

}
