package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.Enrollment;
import com.ctbc.assignment2.controller.web.MyCourseWebController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(controllers = {MyCourseWebController.class})
@AutoConfigureMockMvc(addFilters = false)
public class MyCourseWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testListActiveOnly() throws Exception {
        Enrollment active = new Enrollment();
        CourseBean course = new CourseBean();
        course.setCourseName("Active course");
        active.setCourse(course);
        active.setStatus("ACTIVE");

        Enrollment cancelled = new Enrollment();
        cancelled.setStatus("CANCELLED");

        when(enrollmentService.findByStudent(anyString())).thenReturn(List.of(active, cancelled));

        mockMvc.perform(get("/my-courses")
                .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("my-courses/index"))
                .andExpect(model().attribute("enrollments", hasSize(1)));
    }

    @Test
    public void testListNoEnrollmentsShowsEmpty() throws Exception {
        when(enrollmentService.findByStudent(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/my-courses")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("my-courses/index"))
                .andExpect(model().attribute("enrollments", hasSize(0)));
    }

    @Test
    public void testCancelEnrollmentRedirects() throws Exception {
        mockMvc.perform(post("/my-courses/cancel/5")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("user1", "n/a")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-courses"));

        verify(enrollmentService).cancel("user1", 5L);
    }
}
