package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.Chapter;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.controller.web.ChapterWebController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.ChapterService;
import com.ctbc.assignment2.service.CourseBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {ChapterWebController.class})
@AutoConfigureMockMvc(addFilters = false)
public class ChapterWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChapterService chapterService;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testListChapters() throws Exception {
        CourseBean course = new CourseBean();
        course.setCourseName("Course A");
        when(courseService.findById(1L)).thenReturn(course);
        when(chapterService.findByCourse(1L)).thenReturn(List.of(new Chapter()));

        mockMvc.perform(get("/admin/course/1/chapters"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chapter/list"))
                .andExpect(model().attributeExists("chapters"))
                .andExpect(model().attribute("courseName", "Course A"));
    }

    @Test
    public void testShowForm() throws Exception {
        mockMvc.perform(get("/admin/course/2/chapter/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/chapter/form"))
                .andExpect(model().attributeExists("chapter"))
                .andExpect(model().attribute("courseId", 2L));
    }

    @Test
    public void testSaveChapterRedirects() throws Exception {
        CourseBean course = new CourseBean();
        course.setId(2L);
        when(courseService.findById(2L)).thenReturn(course);
        when(chapterService.save(any(Chapter.class))).thenReturn(new Chapter());

        mockMvc.perform(post("/admin/course/2/chapter/save")
                        .with(csrf())
                        .param("title", "Intro")
                        .param("sortOrder", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/course/2/chapters"));
    }

    @Test
    public void testDeleteChapterRedirects() throws Exception {
        mockMvc.perform(post("/admin/course/2/chapter/delete/5").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/course/2/chapters"));

        verify(chapterService).delete(5L);
    }
}
