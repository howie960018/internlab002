package com.ctbc.assignment2;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.controller.web.CategoryWebController;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.controller.web.CourseWebController;
import com.ctbc.assignment2.controller.web.HomeWebController;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web MVC Controller 行為測試（Thymeleaf 畫面）
 *
 * 測試重點：
 * - Controller 是否正確回傳 View 名稱
 * - Model 是否包含必要資料
 * - 表單 submit 後是否正確 redirect
 *
 * 測試層級：
 * Controller（Web / MVC）+ Mocked Service
 */
@WebMvcTest(controllers = {
        CourseWebController.class,
        CategoryWebController.class,
        HomeWebController.class
})
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    // 固定 UUID，避免 randomUUID() 導致 mock 對不到
    private static final UUID CATEGORY_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID COURSE_ID =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    /**
     * 首頁請求
     * 預期：顯示 home 畫面
     */
    @Test
    void testHomePage_ShouldReturnHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    // ============================================================
    // Category（Web MVC）
    // ============================================================

    /**
     * 顯示分類列表頁
     */
    @Test
    void testCategoryList_ShouldShowListPage() throws Exception {
        
        when(categoryService.findAll())
                .thenReturn(List.of(new CourseCategoryBean()));

        mockMvc.perform(get("/category/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/list"))
                .andExpect(model().attributeExists("categories"));
    }

    /**
     * 顯示新增分類表單頁
     */
    @Test
    void testCategoryForm_ShouldShowFormPage() throws Exception {
        mockMvc.perform(get("/category/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"))
                .andExpect(model().attributeExists("category"));
    }

    /**
     * 新增分類成功後
     * 預期：redirect 回分類列表頁
     */
    @Test
    void testSaveCategory_ShouldRedirectToList() throws Exception {
        mockMvc.perform(post("/category/save")
                .param("categoryName", "新分類"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/category/list"));

        verify(categoryService).save(any(CourseCategoryBean.class));
    }

    /**
     * 編輯分類頁
     * 預期：顯示表單並帶入原本資料
     */
    @Test
    void testEditCategory_ShouldShowFormWithData() throws Exception {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setId(CATEGORY_ID);
        category.setCategoryName("測試分類");

        when(categoryService.findById(CATEGORY_ID))
                .thenReturn(category);

        mockMvc.perform(get("/category/edit/" + CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"))
                .andExpect(model().attributeExists("category"));
    }

    /**
     * 刪除分類
     * 預期：redirect 回分類列表頁
     */
    @Test
    void testDeleteCategory_ShouldRedirectToList() throws Exception {
        mockMvc.perform(get("/category/delete/" + CATEGORY_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/category/list"));

        verify(categoryService).deleteById(CATEGORY_ID);
    }

    // ============================================================
    // Course（Web MVC）
    // ============================================================

    /**
     * 顯示課程列表頁
     */
    @Test
    void testCourseList_ShouldShowListPage() throws Exception {
        when(courseService.findAll())
                .thenReturn(List.of(new CourseBean()));

        mockMvc.perform(get("/course/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/list"))
                .andExpect(model().attributeExists("courses"));
    }

    /**
     * 顯示新增課程表單頁
     */
    @Test
    void testCourseForm_ShouldShowFormPage() throws Exception {
        when(categoryService.findAll())
                .thenReturn(List.of(new CourseCategoryBean()));

        mockMvc.perform(get("/course/form"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("categories"));
    }

    /**
     * 新增課程（指定分類）
     * 預期：redirect 回課程列表頁
     */
    @Test
    void testSaveCourse_WithCategory_ShouldRedirect() throws Exception {
        CourseCategoryBean category = new CourseCategoryBean();
        category.setId(CATEGORY_ID);

        when(categoryService.findById(CATEGORY_ID))
                .thenReturn(category);

        mockMvc.perform(post("/course/save")
                        .param("courseName", "課程 A")
                        .param("price", "1000")
                        .param("categoryId", CATEGORY_ID.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/list"));

        verify(courseService).save(any(CourseBean.class));
    }

    /**
     * 新增課程（不指定分類）
     */
    @Test
    void testSaveCourse_WithoutCategory_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/course/save")
                        .param("courseName", "課程 B")
                        .param("price", "2000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/list"));

        verify(courseService).save(any(CourseBean.class));
    }

    /**
     * 編輯課程頁
     */
    @Test
    void testEditCourse_ShouldShowFormWithData() throws Exception {
        when(courseService.findById(COURSE_ID))
                .thenReturn(new CourseBean());
        when(categoryService.findAll())
                .thenReturn(List.of(new CourseCategoryBean()));

        mockMvc.perform(get("/course/edit/" + COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("categories"));
    }

    /**
     * 刪除課程
     * 預期：redirect 回課程列表頁
     */
    @Test
    void testDeleteCourse_ShouldRedirectToList() throws Exception {
        mockMvc.perform(get("/course/delete/" + COURSE_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/course/list"));

        verify(courseService).deleteById(COURSE_ID);
    }
}