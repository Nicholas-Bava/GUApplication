package guapplication.controller;


import guapplication.Course;
import guapplication.Registration;
import guapplication.service.CourseService;
import guapplication.service.PopulateService;
import guapplication.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
public class WebController {

    private PopulateService populateService;
    private RegisterService registerService;

    private CourseService courseService;

    @Autowired
    public WebController(PopulateService populateService, RegisterService registerService, CourseService courseService){
        this.populateService = populateService;
        this.registerService = registerService;
        this.courseService = courseService;
    }

    @RequestMapping(value = "/courses/populate", method = RequestMethod.GET)
    public ResponseEntity populate(){
        populateService.populate();
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/courses", method = RequestMethod.GET)
    public ResponseEntity<Course> getCourses(){
        return new ResponseEntity(courseService.getAllCourses(), HttpStatus.OK);
    }

    @RequestMapping(value = "/applications", method = RequestMethod.POST)
    public ResponseEntity<HashMap<String,Registration>> registerForCourse(@RequestBody Registration registration, @RequestParam(value="courseName") String courseName){
        return new ResponseEntity(registerService.registerForCourse(registration, courseName), HttpStatus.OK);
    }

    @RequestMapping(value = "/applications", method = RequestMethod.GET)
    public ResponseEntity<List<Registration>> getRegistrations(@RequestParam(value= "courseID", required = false) Long courseID, @RequestParam(value="hashCode", required = false) UUID uuid, @RequestParam(value="email", required = false) String email) {
        if (courseID != null){
            return new ResponseEntity(courseService.getAllApplicantsOfCourse(courseID), HttpStatus.OK);
        } else{
            if (uuid == null){
                return new ResponseEntity<>(registerService.findByEmail(email), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(registerService.findByUuid(uuid), HttpStatus.OK);
            }
        }
    }
}
