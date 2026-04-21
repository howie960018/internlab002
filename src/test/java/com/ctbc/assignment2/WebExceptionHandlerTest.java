package com.ctbc.assignment2;

import com.ctbc.assignment2.controller.web.CategoryWebController;
import com.ctbc.assignment2.controller.web.CourseWebController;
import com.ctbc.assignment2.controller.web.HomeWebController;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.exception.WebExceptionHandler;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

@WebMvcTest(controllers = {
        CourseWebController.class,
        CategoryWebController.class,
        HomeWebController.class
})
@Import(WebExceptionHandler.class)
public class WebExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    private static final UUID NOT_EXIST_UUID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    // =====================================================
    // 404 - 資源找不到
    // =====================================================

    @Test
    public void testWeb404_CourseNotFound_ShouldShowErrorPage() throws Exception {
        when(courseService.findById(NOT_EXIST_UUID))
                .thenThrow(new ResourceNotFoundException(
                        "Course not found: " + NOT_EXIST_UUID));

        mockMvc.perform(get("/course/edit/{id}", NOT_EXIST_UUID))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "Course not found: " + NOT_EXIST_UUID
                ));
    }

    @Test
    public void testWeb404_CategoryNotFound_ShouldShowErrorPage() throws Exception {
        when(categoryService.findById(NOT_EXIST_UUID))
                .thenThrow(new ResourceNotFoundException(
                        "Category not found: " + NOT_EXIST_UUID));

        mockMvc.perform(get("/category/edit/{id}", NOT_EXIST_UUID))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "Category not found: " + NOT_EXIST_UUID
                ));
    }

    /**
     * 預期：導向 error 頁
     */
    @Test
    public void testWeb404_DeleteNonExistingCourse_ShouldShowErrorPage() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Course not found: " + NOT_EXIST_UUID))
                .when(courseService).deleteById(NOT_EXIST_UUID);

        mockMvc.perform(get("/course/delete/{id}", NOT_EXIST_UUID))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    public void testWeb404_DeleteNonExistingCategory_ShouldShowErrorPage() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Category not found: " + NOT_EXIST_UUID))
                .when(categoryService).deleteById(NOT_EXIST_UUID);

        mockMvc.perform(get("/category/delete/{id}", NOT_EXIST_UUID))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // =====================================================
    // 409 - 重複名稱
    // =====================================================

    @Test
    public void testWeb409_DuplicateCourseName_ShouldStayOnCourseForm() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DuplicateCourseNameException("課程名稱已存在：Java 基礎"));

        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                .param("courseName", "Java 基礎")
                .param("price", "3000"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("duplicateError"));
    }

    @Test
    public void testWeb409_DuplicateCategoryName_ShouldStayOnCategoryForm() throws Exception {
        when(categoryService.save(any()))
                .thenThrow(new DuplicateCourseNameException("類別名稱已存在：Java"));

        mockMvc.perform(post("/category/save")
                .param("categoryName", "Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"))
                .andExpect(model().attributeExists("duplicateError"));
    }

    @Test
    public void testWeb409_CategoryDbConstraintViolation_ShouldShowErrorPage() throws Exception {
        when(categoryService.save(any()))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));

        mockMvc.perform(post("/category/save")
                .param("categoryName", "重複類別"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "資料違反資料庫限制，請確認輸入內容"
                ));
    }

    @Test
    public void testWeb409_CourseDbConstraintViolation_ShouldShowErrorPage() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));

        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                .param("courseName", "測試課程")
                .param("price", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "資料違反資料庫限制，請確認輸入內容"
                ));
    }

    // =====================================================
    // 400 - UUID PathVariable 格式錯誤
    // =====================================================

    /**
     * 課程 UUID 格式錯誤
     * 預期：導向 error 頁
     */
    @Test
    public void testWeb400_InvalidCourseUuid_ShouldShowErrorPage() throws Exception {
        mockMvc.perform(get("/course/edit/not-a-uuid"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    /**
     * 類別 UUID 格式錯誤
     * 預期：導向 error 頁
     */
    @Test
    public void testWeb400_InvalidCategoryUuid_ShouldShowErrorPage() throws Exception {
        mockMvc.perform(get("/category/edit/1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    /**
     * 刪除課程時 UUID 格式錯誤
     * 預期：導向 error 頁
     */
    @Test
    public void testWeb400_DeleteCourseInvalidUuid_ShouldShowErrorPage() throws Exception {
        mockMvc.perform(get("/course/delete/xxxx"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // =====================================================
    // Validation - 欄位驗證失敗
    // =====================================================

    /**
     * 課程名稱為空白
     * 預期：留在課程表單頁
     */
    @Test
    public void testWebValidation_CourseNameBlank_ShouldStayOnForm() throws Exception {
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                .param("courseName", "")
                .param("price", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"));
    }

    /**
     * 類別名稱為空白
     * 預期：留在類別表單頁
     */
    @Test
    public void testWebValidation_CategoryNameBlank_ShouldStayOnForm() throws Exception {
        mockMvc.perform(post("/category/save")
                .param("categoryName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"));
    }

    /**
     * 課程價格為負數
     * 預期：留在課程表單頁
     */
    @Test
    public void testWebValidation_NegativePrice_ShouldStayOnForm() throws Exception {
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                .param("courseName", "測試課程")
                .param("price", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"));
    }

    // =====================================================
    // 500 - 未預期例外
    // =====================================================

    @Test
    public void testWeb500_UnexpectedException_ShouldShowErrorPage() throws Exception {
        when(courseService.findAll())
                .thenThrow(new RuntimeException("資料庫連線失敗"));

        mockMvc.perform(get("/course/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "系統發生未預期錯誤：資料庫連線失敗"
                ));
    }

    @Test
    public void testWeb500_CategoryUnexpectedException_ShouldShowErrorPage() throws Exception {
        when(categoryService.findAll())
                .thenThrow(new RuntimeException("NullPointerException"));

        mockMvc.perform(get("/category/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute(
                        "errorMessage",
                        "系統發生未預期錯誤：NullPointerException"
                ));
    }

    // =====================================================
    // 首頁
    // =====================================================

    /**
     * 正常進入首頁
     * 預期：顯示 home 頁面
     */
    @Test
    public void testHome_ShouldDisplayHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }
}