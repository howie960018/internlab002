package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.controller.web.CourseWebController;
import com.ctbc.assignment2.security.SecurityConfig;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CourseWebController.class})
@Import({SecurityConfig.class, TestSecurityBeans.class})
@WithMockUser(roles = "ADMIN")
public class CourseWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @Test
    public void testListHappyPath() throws Exception {
        CourseBean c1 = new CourseBean();
        c1.setId(1L);
        c1.setCourseName("課程A");
        c1.setPrice(100.0);

        CourseBean c2 = new CourseBean();
        c2.setId(2L);
        c2.setCourseName("課程B");
        c2.setPrice(200.0);

        Page<CourseBean> page = new PageImpl<>(
                List.of(c1, c2),
                PageRequest.of(0, 2),
                3
        );

        when(courseService.findPage(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/course/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/list"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 2))
                .andExpect(model().attribute("pageSize", 2));

        System.out.println("✅ testListHappyPath 通過");
    }

    @Test
    public void testDeleteHappyPath() throws Exception {
        doNothing().when(courseService).deleteById(1L);

        mockMvc.perform(post("/course/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/list"));

        System.out.println("✅ testDeleteHappyPath 通過");
    }

    @Test
    public void testDeleteBatchHappyPath() throws Exception {
        doNothing().when(courseService).deleteBatch(any());

        mockMvc.perform(post("/course/deleteBatch")
                .with(csrf())
                        .param("ids", "1", "2", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/list"));

        System.out.println("✅ testDeleteBatchHappyPath 通過");
    }

    @Test
    public void testShowFormHappyPath() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/course/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("categories"));

        System.out.println("✅ testShowFormHappyPath 通過");
    }

    @Test
    public void testEditHappyPath() throws Exception {
        CourseBean course = new CourseBean();
        course.setId(1L);
        course.setCourseName("課程A");
        course.setPrice(100.0);

        when(courseService.findById(1L)).thenReturn(course);
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/course/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("categories"));

        System.out.println("✅ testEditHappyPath 通過");
    }

    @Test
    public void testSaveHappyPath() throws Exception {
        CourseBean saved = new CourseBean();
        saved.setId(1L);
        saved.setCourseName("課程A");
        saved.setPrice(100.0);

        when(courseService.save(any())).thenReturn(saved);

        mockMvc.perform(post("/course/save")
                .with(csrf())
                        .param("courseName", "課程A")
                        .param("price", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/list"));

        System.out.println("✅ testSaveHappyPath 通過");
    }
}
