package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.controller.rest.CourseBeanRestController;
import com.ctbc.assignment2.security.SecurityConfig;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CourseBeanRestController.class})
@Import({SecurityConfig.class, TestSecurityBeans.class})
public class CourseBeanRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @Test
    @WithMockUser(roles = "USER")
    public void testGetByIdHappyPath() throws Exception {
        CourseBean course = new CourseBean();
        course.setId(1L);
        course.setCourseName("API課程");
        course.setPrice(500.0);

        when(courseService.findById(1L)).thenReturn(course);

        mockMvc.perform(get("/api/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.courseName").value("API課程"))
                .andExpect(jsonPath("$.price").value(500.0));

        System.out.println("✅ testGetByIdHappyPath 通過");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteHappyPath() throws Exception {
        doNothing().when(courseService).deleteById(1L);

        mockMvc.perform(delete("/api/course/1"))
                .andExpect(status().isOk());

        System.out.println("✅ testDeleteHappyPath 通過");
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllHappyPath() throws Exception {
        CourseBean c1 = new CourseBean();
        c1.setId(1L);
        c1.setCourseName("課程A");
        c1.setPrice(100.0);

        when(courseService.findAll()).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseName").value("課程A"));

        System.out.println("✅ testGetAllHappyPath 通過");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSaveHappyPath() throws Exception {
        CourseBean saved = new CourseBean();
        saved.setId(1L);
        saved.setCourseName("API課程");
        saved.setPrice(500.0);

        when(courseService.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"API課程\",\"price\":500.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("API課程"));

        System.out.println("✅ testSaveHappyPath 通過");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSaveWithCategoryHappyPath() throws Exception {
        com.ctbc.assignment2.bean.CourseCategoryBean category = new com.ctbc.assignment2.bean.CourseCategoryBean();
        category.setId(2L);
        category.setCategoryName("分類A");

        CourseBean saved = new CourseBean();
        saved.setId(10L);
        saved.setCourseName("API課程分類");
        saved.setPrice(300.0);
        saved.setCategory(category);

        when(categoryService.findById(2L)).thenReturn(category);
        when(courseService.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/course/category/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"API課程分類\",\"price\":300.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("API課程分類"))
                .andExpect(jsonPath("$.category.categoryName").value("分類A"));

        System.out.println("✅ testSaveWithCategoryHappyPath 通過");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testBatchSaveHappyPath() throws Exception {
        CourseBean c1 = new CourseBean();
        c1.setId(1L);
        c1.setCourseName("批次API課程1");
        c1.setPrice(100.0);

        CourseBean c2 = new CourseBean();
        c2.setId(2L);
        c2.setCourseName("批次API課程2");
        c2.setPrice(200.0);

        when(courseService.saveBatch(any())).thenReturn(List.of(c1, c2));

        mockMvc.perform(post("/api/course/batch")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"courseName\":\"批次API課程1\",\"price\":100.0},{\"courseName\":\"批次API課程2\",\"price\":200.0}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseName").value("批次API課程1"))
                .andExpect(jsonPath("$[1].courseName").value("批次API課程2"));

        System.out.println("✅ testBatchSaveHappyPath 通過");
    }
}
