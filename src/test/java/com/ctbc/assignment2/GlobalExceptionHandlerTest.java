package com.ctbc.assignment2;

import com.ctbc.assignment2.controller.rest.CategoryBeanRestController;
import com.ctbc.assignment2.controller.rest.CourseBeanRestController;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.GlobalExceptionHandler;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 專門負責測試我們的【全域 API 錯誤處理器 (GlobalExceptionHandler)】是否有發揮作用。
 * 【初學者觀念】：
 *   @WebMvcTest : 這個註解不會啟動一整個 Spring 環境，而只會把 Controller 準備好，
 *                 非常適合「只測網頁或 API 介面通訊」的純輕量級測試。
 *   @MockBean   : 我們不想真的連接資料庫做複雜存取，所以用 Mock 假人代替 Service，
 *                 好讓我們可以隨心所欲控制它「無論呼叫什麼都丟出 NotFoundException」，以觸發例外。
 */
/**
 * GlobalExceptionHandler 行為驗證測試（REST API, UUID 版本）
 *
 * 測試重點：
 * - REST Controller 發生例外時，是否正確轉換為 HTTP Status
 * - 回傳的 ErrorResponse JSON 格式是否一致
 * - UUID PathVariable、RequestBody 錯誤是否正確攔截
 *
 * 測試層級：Controller + GlobalExceptionHandler
 */
@WebMvcTest(controllers = {
        CourseBeanRestController.class,
        CategoryBeanRestController.class
})
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    private static final UUID UUID_1 =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID UUID_2 =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    // =====================================================
    // 404 Not Found
    // =====================================================

    /**
     * GET 課程：查詢不存在的課程 ID
     * 預期：回傳 404 Not Found + ErrorResponse
     */
    @Test
    void testGetCourse_NotFound_ShouldReturn404() throws Exception {
        when(courseService.findById(UUID_1))
                .thenThrow(new ResourceNotFoundException("Course not found: " + UUID_1));

        mockMvc.perform(get("/api/course/" + UUID_1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Course not found: " + UUID_1))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").exists());
    }

    /**
     * DELETE 課程：刪除不存在的課程
     * 預期：回傳 404 Not Found
     */
    @Test
    void testDeleteCourse_NotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Course not found: " + UUID_1))
                .when(courseService).deleteById(UUID_1);

        mockMvc.perform(delete("/api/course/" + UUID_1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Course not found: " + UUID_1));
    }

    /**
     * GET 分類：查詢不存在的分類
     * 預期：回傳 404 Not Found
     */
    @Test
    void testGetCategory_NotFound_ShouldReturn404() throws Exception {
        when(categoryService.findById(UUID_1))
                .thenThrow(new ResourceNotFoundException("Category not found: " + UUID_1));

        mockMvc.perform(get("/api/category/" + UUID_1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Category not found: " + UUID_1));
    }

    /**
     * DELETE 分類：刪除不存在的分類
     * 預期：回傳 404 Not Found
     */
    @Test
    void testDeleteCategory_NotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found: " + UUID_1))
                .when(categoryService).deleteById(UUID_1);

        mockMvc.perform(delete("/api/category/" + UUID_1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Category not found: " + UUID_1));
    }

    // =====================================================
    // 409 Conflict
    // =====================================================

    /**
     * 新增課程：課程名稱重複
     * 預期：回傳 409 Conflict
     */
    @Test
    void testCreateCourse_DuplicateName_ShouldReturn409() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DuplicateCourseNameException("Duplicate course name"));

        mockMvc.perform(post("/api/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseName\":\"Java\",\"price\":100}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 新增分類：分類名稱重複
     * 預期：回傳 409 Conflict
     */
    @Test
    void testCreateCategory_DuplicateName_ShouldReturn409() throws Exception {
        when(categoryService.save(any()))
                .thenThrow(new DuplicateCourseNameException("Duplicate category name"));

        mockMvc.perform(post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryName\":\"Web\"}"))
                .andExpect(status().isConflict());
    }

    /**
     * 資料庫 constraint 違反
     * 預期：回傳 409 Conflict
     */
    @Test
    void testDatabaseConstraintViolation_ShouldReturn409() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DataIntegrityViolationException("constraint"));

        mockMvc.perform(post("/api/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseName\":\"Test\",\"price\":100}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    // =====================================================
    // 400 Bad Request
    // =====================================================

    /**
     * 課程 UUID PathVariable 格式錯誤
     * 預期：回傳 400 Bad Request
     */
    @Test
    void testGetCourse_InvalidUuid_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/course/abc"))
                .andExpect(status().isBadRequest());
    }

    /**
     * 分類 UUID PathVariable 格式錯誤
     */
    @Test
    void testGetCategory_InvalidUuid_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/category/abc"))
                .andExpect(status().isBadRequest());
    }

    /**
     * RequestBody 缺少 courseName
     */
    @Test
    void testCreateCourse_MissingCourseName_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"price\":100}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCourse_MissingPrice_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courseName\":\"Test\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * JSON 格式錯誤
     */
    @Test
    void testCreateCourse_MalformedJson_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not-json"))
                .andExpect(status().isBadRequest());
    }

    // =====================================================
    // 415 Unsupported Media Type
    // =====================================================

    /**
     * 缺少 Content-Type header
     */
    @Test
    void testCreateCourse_MissingContentType_ShouldReturn415() throws Exception {
        mockMvc.perform(post("/api/course")
                .content("{\"courseName\":\"Test\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // =====================================================
    // 500 Internal Server Error
    // =====================================================

    /**
     * 未預期 RuntimeException（課程查詢）
     */
    @Test
    void testUnexpectedCourseException_ShouldReturn500() throws Exception {
        when(courseService.findById(UUID_2))
                .thenThrow(new RuntimeException("Boom"));

        mockMvc.perform(get("/api/course/" + UUID_2))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Boom"));
    }

    /**
     * 未預期 RuntimeException（分類查詢）
     */
    @Test
    void testUnexpectedCategoryException_ShouldReturn500() throws Exception {
        when(categoryService.findAll())
                .thenThrow(new RuntimeException("Category DB error"));

        mockMvc.perform(get("/api/category/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Category DB error"));
    }

    // =====================================================
    // ErrorResponse 結構驗證
    // =====================================================

    /**
     * ErrorResponse 應包含 timestamp / message / details
     */
    @Test
    void testErrorResponse_ShouldContainAllFields() throws Exception {
        when(courseService.findById(UUID_1))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/course/" + UUID_1))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details").exists());
    }

    /**
     * ErrorResponse.details 應包含 request URI
     */
    @Test
    void testErrorResponseDetails_ShouldContainRequestUri() throws Exception {
        when(courseService.findById(UUID_1))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/course/" + UUID_1))
                .andExpect(jsonPath("$.details")
                        .value(org.hamcrest.Matchers
                                .containsString("/api/course/" + UUID_1)));
    }

    // =====================================================
    // 正常流程
    // =====================================================

    /**
     * 正常取得課程列表
     */
    @Test
    void testGetAllCourses_ShouldReturn200() throws Exception {
        when(courseService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isOk());
    }
}