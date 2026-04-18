package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseStatus;
import com.ctbc.assignment2.controller.web.InstructorDashboardController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import com.ctbc.assignment2.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(controllers = {InstructorDashboardController.class})
@AutoConfigureMockMvc(addFilters = false)
public class InstructorDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @MockBean
    private FileStorageService fileStorageService;

        @MockBean
        private JwtService jwtService;

    @Test
    public void testDashboardPublishedCount() throws Exception {
        CourseBean draft = new CourseBean();
        draft.setStatus(CourseStatus.DRAFT);
        CourseBean published = new CourseBean();
        published.setStatus(CourseStatus.PUBLISHED);

        when(courseService.findByInstructorName("instructor1"))
                .thenReturn(List.of(draft, published));

        mockMvc.perform(get("/instructor/dashboard")
                        .principal(new UsernamePasswordAuthenticationToken("instructor1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/dashboard"))
                .andExpect(model().attribute("publishedCount", 1L));
    }

            @Test
            public void testDashboardPublishedCountEmptyCourses() throws Exception {
            when(courseService.findByInstructorName("instructor1"))
                .thenReturn(List.of());

            mockMvc.perform(get("/instructor/dashboard")
                    .principal(new UsernamePasswordAuthenticationToken("instructor1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/dashboard"))
                .andExpect(model().attribute("publishedCount", 0L));
            }

            @Test
            public void testDashboardIgnoresNullStatus() throws Exception {
            CourseBean nullStatus = new CourseBean();
            CourseBean published = new CourseBean();
            published.setStatus(CourseStatus.PUBLISHED);

            when(courseService.findByInstructorName("instructor1"))
                .thenReturn(List.of(nullStatus, published));

            mockMvc.perform(get("/instructor/dashboard")
                    .principal(new UsernamePasswordAuthenticationToken("instructor1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/dashboard"))
                .andExpect(model().attribute("publishedCount", 1L));
            }

    @Test
    public void testListCourses() throws Exception {
        when(courseService.findByInstructorName("instructor1"))
                .thenReturn(List.of(new CourseBean()));

        mockMvc.perform(get("/instructor/courses")
                        .principal(new UsernamePasswordAuthenticationToken("instructor1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/course/list"))
                .andExpect(model().attributeExists("courses"));
    }

    @Test
    public void testShowFormSetsInstructor() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/instructor/course/form")
                .with(csrf())
                .principal(new UsernamePasswordAuthenticationToken("instructor1", "n/a")))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/course/form"))
                .andExpect(model().attribute("course", hasProperty("instructorName", is("instructor1"))))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    public void testSaveCourseHappyPath() throws Exception {
        when(courseService.save(any())).thenReturn(new CourseBean());

        mockMvc.perform(post("/instructor/course/save")
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken("instructor1", "n/a"))
                        .param("courseName", "Test course")
                        .param("price", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/courses"));

                    verify(courseService).save(any());
    }
}
