package org.example.course.endpoints;


import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.course.services.payloads.requests.CourseRequest;
import org.example.course.services.payloads.responses.CourseResponse;
import org.example.course.services.CourseService;
import org.example.course.services.payloads.requests.CourseUpdateRequest;
import org.example.course.domains.Course;
import org.example.course.domains.CourseRepository;

@Path("/course")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Course Management Module", description = "course Management")

public class CourseController {


    @Inject
    CourseService courseService;

    @Inject
    CourseRepository courseRepository;

    @POST
    @Path("/create-new-course")
    @Transactional
    @Operation(summary = "create new course", description = "create new course.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    public Response createNewCourse(CourseRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,courseService.createNewCourse(request))).build();
    }

    @GET
    @Path("/get-all-courses")
    @Transactional
    @Operation(summary = "get all courses", description = "get all courses.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Course.class)))
    public Response getAllCourses() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,courseService.getAllCourses())).build();
    }

    @PUT
    @Path("/update-course/{id}")
    @Transactional
    @Operation(summary = "Update course by Id", description = "Update course by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Course.class)))
    public Response updateCourse(@PathParam("id") Long id, CourseUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,courseService.updateCourseById(id, request) )).build();
    }
}






