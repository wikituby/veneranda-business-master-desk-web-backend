package org.example.course.services;

import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.example.auth.services.UserAuthService;
import org.example.course.domains.Course;
import org.example.course.domains.CourseRepository;
import org.example.course.services.payloads.requests.CourseRequest;
import org.example.course.services.payloads.requests.CourseUpdateRequest;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CourseService {
    @Inject
    CourseRepository courseRepository;

    @Inject
    Pool client;

    @Inject
    UserAuthService userAuthService;

    private static final String NOT_FOUND = "Not found!";

    public Course createNewCourse(CourseRequest request){
        Course course = new Course();
        course.title = request.title;
        course.description = request.description;
        course.details = request.details;
        course.image = request.image;
        course.parent = request.parent;
        course.creationDate = LocalDate.now();


        courseRepository.persist(course);
        return course;
    }

    public List<Course> getAllCourses() {
        return courseRepository.listAll();
    }


    public Course updateCourseById(Long id, CourseUpdateRequest request) {
        return courseRepository.findByIdOptional(id)
                .map(course -> {

                    course.title = request.title;
                    course.description = request.description;
                    course.details = request.details;
                    course.image = request.image;
                    course.parent = request.parent;
                    course.lastUpdateDate = LocalDate.now();

                    courseRepository.persist(course);
                    return course;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }
}






