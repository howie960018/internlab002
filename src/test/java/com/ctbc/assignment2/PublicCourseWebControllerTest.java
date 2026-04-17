package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.controller.web.PublicCourseWebController;
import com.ctbc.assignment2.security.JwtService;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {PublicCourseWebController.class})
@AutoConfigureMockMvc(addFilters = false)
public class PublicCourseWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    @MockBean
    private JwtService jwtService;

    @Test
    public void testBrowseAllCourses() throws Exception {
        stubCategoryTree();
        Page<CourseBean> page = new PageImpl<>(List.of(), PageRequest.of(0, 9), 0);
        when(courseService.findPage(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/index"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 0))
                .andExpect(model().attribute("pageSize", 9));
    }

    @Test
    public void testBrowseByCategory() throws Exception {
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(1L);
        parent.setCategoryName("主類別");

        CourseCategoryBean child = new CourseCategoryBean();
        child.setId(2L);
        child.setCategoryName("子類別");
        child.setParent(parent);

        when(categoryService.findTopLevel()).thenReturn(List.of(parent));
        when(categoryService.findChildren(1L)).thenReturn(List.of(child));
        when(categoryService.findById(1L)).thenReturn(parent);
        Page<CourseBean> page = new PageImpl<>(List.of(), PageRequest.of(0, 9), 0);
        when(courseService.findPageByCategoryIds(anyList(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/courses").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/index"))
                .andExpect(model().attributeExists("selectedCategory"))
                .andExpect(model().attributeExists("courses"));
    }

    @Test
    public void testBrowseByKeyword() throws Exception {
        stubCategoryTree();
        Page<CourseBean> page = new PageImpl<>(List.of(), PageRequest.of(0, 9), 0);
        when(courseService.findPageByName(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/courses").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/index"))
                .andExpect(model().attribute("query", "java"));
    }

    @Test
    public void testCourseDetail() throws Exception {
        CourseBean course = new CourseBean();
        course.setId(1L);
        course.setCourseName("Course A");
        course.setPrice(100.0);

        when(courseService.findById(1L)).thenReturn(course);

        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/detail"))
                .andExpect(model().attributeExists("course"));
    }

    private void stubCategoryTree() {
        CourseCategoryBean parent = new CourseCategoryBean();
        parent.setId(1L);
        parent.setCategoryName("主類別");

        when(categoryService.findTopLevel()).thenReturn(List.of(parent));
        when(categoryService.findChildren(anyLong())).thenReturn(List.of());
    }
}
