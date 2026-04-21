package com.ctbc.assignment2;


import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.controller.rest.CategoryBeanRestController;
import com.ctbc.assignment2.controller.rest.CourseBeanRestController;
import com.ctbc.assignment2.exception.GlobalExceptionHandler;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST Controller 行為測試
 *
 * 測試範圍：
 *   - Course / Category REST API 是否正確回傳 JSON
 *   - HTTP method 與 endpoint 是否如設計
 *   - Service 是否被正確呼叫
 *
 * 測試層級：Controller（REST）+ GlobalExceptionHandler
 */
@WebMvcTest(controllers = {
        CourseBeanRestController.class,
        CategoryBeanRestController.class
})
@Import(GlobalExceptionHandler.class)
class RestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    private static final UUID CAT_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID COURSE_ID_1 =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private static final UUID COURSE_ID_2 =
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private CourseCategoryBean category;
    private CourseBean course1;
    private CourseBean course2;

    @BeforeEach
    void setUp() {
        category = new CourseCategoryBean();
        category.setId(CAT_ID);
        category.setCategoryName("程式設計");

        course1 = new CourseBean();
        course1.setId(COURSE_ID_1);
        course1.setCourseName("Java 基礎");
        course1.setPrice(3000.0);
        course1.setCategory(category);

        course2 = new CourseBean();
        course2.setId(COURSE_ID_2);
        course2.setCourseName("Spring Boot 入門");
        course2.setPrice(5000.0);
        course2.setCategory(category);
    }

    // =====================================================
    // Course API
    // =====================================================

    /**
     * 查詢所有課程 預期：回傳課程清單 JSON，筆數正確
     */
    @Test
    void testGetAllCourses_ShouldReturnCourseList() throws Exception {
        when(courseService.findAll()).thenReturn(List.of(course1, course2));

        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    /**
     * 依 ID 查詢單一課程
     */
    @Test
    void testGetCourseById_ShouldReturnCourse() throws Exception {
        when(courseService.findById(COURSE_ID_1)).thenReturn(course1);

        mockMvc.perform(get("/api/course/" + COURSE_ID_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Java 基礎"));
    }

    /**
     * 刪除課程 預期：HTTP 200，Service deleteById 被呼叫
     */
    @Test
    void testDeleteCourseById_ShouldInvokeService() throws Exception {
        doNothing().when(courseService).deleteById(COURSE_ID_1);

        mockMvc.perform(delete("/api/course/" + COURSE_ID_1))
                .andExpect(status().isOk());

        verify(courseService).deleteById(COURSE_ID_1);
    }

    /**
     * 新增課程（不指定分類）
     */
    @Test
    void testCreateCourse_ShouldReturnSavedCourse() throws Exception {
        when(courseService.save(any(CourseBean.class))).thenReturn(course1);

        mockMvc.perform(post("/api/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Java 基礎"));
    }

    /**
     * 新增課程並指定分類
     */
    @Test
    void testCreateCourseWithCategory_ShouldReturnCourseWithCategory() throws Exception {
        when(categoryService.findById(CAT_ID)).thenReturn(category);
        when(courseService.save(any(CourseBean.class))).thenReturn(course1);

        mockMvc.perform(post("/api/course/category/" + CAT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.categoryName").value("程式設計"));
    }

    @Test
    void shouldUpdateCourseByPut() throws Exception {
        CourseBean updated = new CourseBean();
        updated.setId(COURSE_ID_1);
        updated.setCourseName("Java 進階");
        updated.setPrice(4000.0);

        when(courseService.save(any(CourseBean.class))).thenReturn(updated);

        mockMvc.perform(put("/api/course/" + COURSE_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Java 進階"))
                .andExpect(jsonPath("$.price").value(4000.0));

        verify(courseService).save(any(CourseBean.class));
    }

    @Test
    void shouldPatchCoursePrice() throws Exception {
        CourseBean existing = course1;
        existing.setPrice(6000.0);

        when(courseService.findById(COURSE_ID_1)).thenReturn(course1);
        when(courseService.save(any(CourseBean.class))).thenReturn(existing);

        mockMvc.perform(patch("/api/course/" + COURSE_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "price": 6000 }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(6000.0));
    }

    // =====================================================
    // Category API
    // =====================================================

    /**
     * 查詢所有分類
     */
    @Test
    void testGetAllCategories_ShouldReturnCategoryList() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/category/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("程式設計"));
    }

    /**
     * 依 ID 查詢分類
     */
    @Test
    void testGetCategoryById_ShouldReturnCategory() throws Exception {
        when(categoryService.findById(CAT_ID)).thenReturn(category);

        mockMvc.perform(get("/api/category/" + CAT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("程式設計"));
    }

    /**
     * 新增分類
     */
    @Test
    void testCreateCategory_ShouldReturnSavedCategory() throws Exception {
        when(categoryService.save(any(CourseCategoryBean.class))).thenReturn(category);

        mockMvc.perform(post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("程式設計"));
    }

    /**
     * 刪除分類
     */
    @Test
    void testDeleteCategoryById_ShouldInvokeService() throws Exception {
        doNothing().when(categoryService).deleteById(CAT_ID);

        mockMvc.perform(delete("/api/category/" + CAT_ID))
                .andExpect(status().isOk());

        verify(categoryService).deleteById(CAT_ID);
    }

    @Test
    void shouldUpdateCategoryByPut() throws Exception {
        CourseCategoryBean updated = new CourseCategoryBean();
        updated.setId(CAT_ID);
        updated.setCategoryName("後端進階課程");

        when(categoryService.save(any(CourseCategoryBean.class))).thenReturn(updated);

        mockMvc.perform(put("/api/category/" + CAT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("後端進階課程"));

        verify(categoryService).save(any(CourseCategoryBean.class));
    }
}