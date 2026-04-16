package com.ctbc.assignment2;

import com.ctbc.assignment2.controller.rest.CategoryBeanRestController;
import com.ctbc.assignment2.controller.rest.CourseBeanRestController;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.GlobalExceptionHandler;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.security.SecurityConfig;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest: 專門用來測試 Web 層 (Controller) 的註解。這會帶來輕量級的啟動，只載入與 MVC 相關的元件，不會載入整個 Service 或 DB 層。
@WebMvcTest(controllers = {
        CourseBeanRestController.class,
        CategoryBeanRestController.class
})
@Import({GlobalExceptionHandler.class, SecurityConfig.class, TestSecurityBeans.class})
@WithMockUser(roles = "ADMIN")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc 是 Spring Test 提供來模擬 HTTP 請求與驗證回應的工具

    // @MockBean: 在 Spring 容器中建立並注入一個 Mock (假) 物件，這樣 Controller 就能使用它，而不依賴真實的資料庫。
    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    // ════════════════════════════════════════════════════
    //   404 ResourceNotFoundException
    // ════════════════════════════════════════════════════

    // @Test: Test 標記
    @Test
        public void test404GetMissingCourse() throws Exception {
        // when(...).thenThrow(...): Mockito 語法。設定「當呼叫 courseService.findById(99999L)」時，強制丟出 ResourceNotFoundException
        when(courseService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Course not found: 99999"));

        mockMvc.perform(get("/api/course/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found: 99999"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").exists());

        System.out.println("✅ test404_查詢不存在的課程 通過");
    }

    @Test
        public void test404DeleteMissingCourse() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Course not found: 99999"))
                .when(courseService).deleteById(99999L);

        mockMvc.perform(delete("/api/course/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found: 99999"));

        System.out.println("✅ test404_刪除不存在的課程 通過");
    }

    @Test
        public void test404GetMissingCategory() throws Exception {
        when(categoryService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Category not found: 99999"));

        mockMvc.perform(get("/api/category/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test404_查詢不存在的類別 通過");
    }

    @Test
        public void test404DeleteMissingCategory() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Category not found: 5"))
                .when(categoryService).deleteById(5L);

        mockMvc.perform(delete("/api/category/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found: 5"));

        System.out.println("✅ test404_刪除不存在的類別 通過");
    }

    // ════════════════════════════════════════════════════
    //   409 DuplicateCourseNameException
    // ════════════════════════════════════════════════════

    @Test
        public void test409CreateDuplicateCourseName() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DuplicateCourseNameException("課程名稱已存在：Java 基礎"));

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Java 基礎\",\"price\":3000.0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("課程名稱已存在：Java 基礎"))
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test409_新增重複課程名稱 通過");
    }

    @Test
        public void test409UpdateToDuplicateCourseName() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DuplicateCourseNameException("課程名稱已存在：Spring Boot 入門"));

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"courseName\":\"Spring Boot 入門\",\"price\":5000.0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("課程名稱已存在：Spring Boot 入門"));

        System.out.println("✅ test409_更新成重複課程名稱 通過");
    }

    @Test
        public void test409CreateDuplicateCategoryName() throws Exception {
        when(categoryService.save(any()))
                .thenThrow(new DuplicateCourseNameException("類別名稱已存在：程式設計"));

        mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"程式設計\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("類別名稱已存在：程式設計"));

        System.out.println("✅ test409_新增重複類別名稱 通過");
    }

    // ════════════════════════════════════════════════════
    //   409 DataIntegrityViolationException
    // ════════════════════════════════════════════════════

    @Test
        public void test409DbConstraintViolation() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"測試課程\",\"price\":100.0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("資料違反資料庫限制，請確認輸入內容"));

        System.out.println("✅ test409_DB_constraint違反 通過");
    }

    // ════════════════════════════════════════════════════
    //   400 MethodArgumentTypeMismatchException
    // ════════════════════════════════════════════════════

    @Test
        public void test400PathVariableTypeMismatchTextToNumber() throws Exception {
        mockMvc.perform(get("/api/course/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test400_PathVariable型態不符_文字傳入數字欄位 通過");
    }

    @Test
        public void test400PathVariableTypeMismatchOnDelete() throws Exception {
        mockMvc.perform(delete("/api/course/xyz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_PathVariable型態不符_刪除時傳入文字 通過");
    }

    @Test
        public void test400CategoryPathVariableTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/category/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_類別PathVariable型態不符 通過");
    }

    // ════════════════════════════════════════════════════
    //   400 Bean Validation (@Valid @RequestBody)
    // ════════════════════════════════════════════════════

    @Test
        public void test400RequestBodyCourseNameBlank() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"\",\"price\":100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_RequestBody_courseName空白 通過");
    }

    @Test
        public void test400RequestBodyPriceNegative() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"測試課程\",\"price\":-1.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_RequestBody_price為負數 通過");
    }

    // ════════════════════════════════════════════════════
    //   400 HttpMessageNotReadableException
    // ════════════════════════════════════════════════════

    @Test
        public void test400JsonMalformedPriceNotNumber() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"測試\",\"price\":\"not-a-number\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test400_JSON格式錯誤_price應為數字 通過");
    }

    @Test
        public void test400JsonCompletelyInvalid() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("this is not json at all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_JSON格式完全不合法 通過");
    }

    @Test
        public void test400EmptyBody() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test400_Body為空 通過");
    }

    // ════════════════════════════════════════════════════
    //   405 HttpRequestMethodNotSupportedException
    //
    //   【修正說明】
    //   本專案 CourseBeanRestController 的完整路由：
    //     GET    /api/course/all
    //     GET    /api/course/{id}
    //     DELETE /api/course/{id}
    //     POST   /api/course
    //     POST   /api/course/category/{categoryId}   ← 這條讓 /api/course/* 的 POST 都有匹配
    //
    //   CategoryBeanRestController：
    //     GET    /api/category/all
    //     GET    /api/category/{id}
    //     DELETE /api/category/{id}
    //     POST   /api/category
    //
    //   真正沒有對應方法、會觸發 405 的路徑：
    //   → /api/course/{id}  只有 GET + DELETE，送 PUT → 405 ✅
    //   → /api/category/{id} 只有 GET + DELETE，送 PUT → 405 ✅
    //   → /api/category/{id} 只有 GET + DELETE，送 PATCH → 405 ✅
    // ════════════════════════════════════════════════════

    @Test
        public void test405PutToCourseId() throws Exception {
        // /api/course/{id} 只有 GET + DELETE，送 PUT → 405
        mockMvc.perform(put("/api/course/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"test\",\"price\":100.0}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test405_對course_id_送PUT 通過");
    }

    @Test
        public void test405PutToCategoryId() throws Exception {
        // /api/category/{id} 只有 GET + DELETE，送 PUT → 405
        mockMvc.perform(put("/api/category/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryName\":\"test\"}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test405_對category_id_送PUT 通過");
    }

    @Test
        public void test405PatchToCategoryId() throws Exception {
        // /api/category/{id} 只有 GET + DELETE，送 PATCH → 405
        mockMvc.perform(patch("/api/category/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test405_對category_id_送PATCH 通過");
    }

    // ════════════════════════════════════════════════════
    //   415 HttpMediaTypeNotSupportedException
    // ════════════════════════════════════════════════════

    @Test
        public void test415PostMissingContentType() throws Exception {
        mockMvc.perform(post("/api/course")
                        .content("{\"courseName\":\"測試\",\"price\":100.0}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test415_POST沒帶ContentType 通過");
    }

    @Test
        public void test415ContentTypePlainText() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("courseName=測試"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test415_ContentType為純文字 通過");
    }

    // ════════════════════════════════════════════════════
    //   500 Exception catch-all
    // ════════════════════════════════════════════════════

    @Test
        public void test500UnexpectedError() throws Exception {
        when(courseService.findById(42L))
                .thenThrow(new RuntimeException("Something bad happened"));

        mockMvc.perform(get("/api/course/42"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something bad happened"))
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test500_未預期例外 通過");
    }

    @Test
        public void test500FindAllThrowsException() throws Exception {
        when(courseService.findAll())
                .thenThrow(new RuntimeException("DB connection failed"));

        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("DB connection failed"));

        System.out.println("✅ test500_findAll拋出例外 通過");
    }

    // ════════════════════════════════════════════════════
    //   ErrorResponse 結構完整性驗証
    // ════════════════════════════════════════════════════

    @Test
        public void testErrorResponseHasAllFields() throws Exception {
        when(courseService.findById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Course not found: 1"));

        mockMvc.perform(get("/api/course/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details").exists());

        System.out.println("✅ testErrorResponse_三個欄位都存在 通過");
    }

    @Test
        public void testErrorResponseDetailsContainsUri() throws Exception {
        when(courseService.findById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Course not found: 7"));

        mockMvc.perform(get("/api/course/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("/api/course/7")));

        System.out.println("✅ testErrorResponse_details包含uri資訊 通過");
    }

    @Test
        public void testGetAllReturnsOk() throws Exception {
        when(courseService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isOk());

        System.out.println("✅ testGetAll_正常回傳 通過");
    }
}