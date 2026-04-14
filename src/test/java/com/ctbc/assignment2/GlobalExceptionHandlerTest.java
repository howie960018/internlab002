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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
@WebMvcTest(controllers = {
        CourseBeanRestController.class,
        CategoryBeanRestController.class
})
@Import(GlobalExceptionHandler.class) // 把我們要測試的「主角」主動引進來
public class GlobalExceptionHandlerTest {

    // MockMvc 是用來模擬發送 Http Request (GET, POST等) 的好用機器人
    @Autowired
    private MockMvc mockMvc;

    // 將我們的 Service 換成 Mock 物件，讓我們後續能使用 when().thenThrow()
    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    // ════════════════════════════════════════════════════
    //   404 ResourceNotFoundException
    // ════════════════════════════════════════════════════

    /**
     * 測試：當打 API 卻找不到課程時，是否能正確拿到 404 Http Status 與我們客製的錯誤 JSON？
     */
    @Test
    public void test404_查詢不存在的課程() throws Exception {
        // 設定假人(Mock)劇本：只要你呼叫找 ID=99999 的，我就絕對丟出 Exception 嚇你
        when(courseService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Course not found: 99999"));

        // 模擬使用 Postman 發送 GET 去 /api/course/99999
        mockMvc.perform(get("/api/course/99999"))
                .andExpect(status().isNotFound()) // 預期它會給 404 (因為 GlobalExceptionHandler 寫了 @ResponseStatus(HttpStatus.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Course not found: 99999")) // 用 jsonPath 檢查回傳的 JSON 裡面的 message 內容
                .andExpect(jsonPath("$.timestamp").exists()) // 預期要有我們自定義回傳的好看 timestamp
                .andExpect(jsonPath("$.details").exists()); // 預期要有請求路徑 details

        System.out.println("✅ test404_查詢不存在的課程 通過");
    }

    @Test
    public void test404_刪除不存在的課程() throws Exception {
        // 當 deleteById 方法不回傳東西(void)時，Mockito 設定假人的寫法是 doThrow().when()
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Course not found: 99999"))
                .when(courseService).deleteById(99999L);

        mockMvc.perform(delete("/api/course/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found: 99999"));

        System.out.println("✅ test404_刪除不存在的課程 通過");
    }

    @Test
    public void test404_查詢不存在的類別() throws Exception {
        when(categoryService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Category not found: 99999"));

        mockMvc.perform(get("/api/category/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found: 99999"))
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test404_查詢不存在的類別 通過");
    }

    @Test
    public void test404_刪除不存在的類別() throws Exception {
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
    public void test409_新增重複課程名稱() throws Exception {
        // 假動作：只要一呼叫 save 就拋重複名稱錯誤
        when(courseService.save(any()))
                .thenThrow(new DuplicateCourseNameException("課程名稱已存在：Java 基礎"));

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Java 基礎\",\"price\":3000.0}")) // 塞假 payload
                .andExpect(status().isConflict()) // isConflict 就是 409
                .andExpect(jsonPath("$.message").value("課程名稱已存在：Java 基礎"))
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test409_新增重複課程名稱 通過");
    }

    @Test
    public void test409_更新成重複課程名稱() throws Exception {
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
    public void test409_新增重複類別名稱() throws Exception {
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
    public void test409_DB_constraint違反() throws Exception {
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

    /**
     * 測試：當 Controller 的參數要求是 Long (數字 ID)，但某個呆瓜傳了英文字母 "abc" 怎麼辦？
     * 我們的 GlobalExceptionHandler 必須攔截到 TypeMismatchException 把狀態轉成 400 Bad Request 回應回去。
     */
    @Test
    public void test400_PathVariable型態不符_文字傳入數字欄位() throws Exception {
        mockMvc.perform(get("/api/course/abc"))
                .andExpect(status().isBadRequest()) // 就是 HTTP 狀態碼 400
                .andExpect(jsonPath("$.message").exists()) // 至少確保有噴一個字串給前端說明原因
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test400_PathVariable型態不符_文字傳入數字欄位 通過");
    }

    @Test
    public void test400_PathVariable型態不符_刪除時傳入文字() throws Exception {
        mockMvc.perform(delete("/api/course/xyz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_PathVariable型態不符_刪除時傳入文字 通過");
    }

    @Test
    public void test400_類別PathVariable型態不符() throws Exception {
        mockMvc.perform(get("/api/category/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_類別PathVariable型態不符 通過");
    }

    // ════════════════════════════════════════════════════
    //   400 Bean Validation (@Valid @RequestBody)
    // ════════════════════════════════════════════════════

    @Test
    public void test400_RequestBody_courseName空白() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"\",\"price\":100.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_RequestBody_courseName空白 通過");
    }

    @Test
    public void test400_RequestBody_price為負數() throws Exception {
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
    public void test400_JSON格式錯誤_price應為數字() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"測試\",\"price\":\"not-a-number\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test400_JSON格式錯誤_price應為數字 通過");
    }

    @Test
    public void test400_JSON格式完全不合法() throws Exception {
        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("this is not json at all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test400_JSON格式完全不合法 通過");
    }

    @Test
    public void test400_Body為空() throws Exception {
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
    public void test405_對course_id_送PUT() throws Exception {
        // /api/course/{id} 只有 GET + DELETE，送 PUT → 405
        mockMvc.perform(put("/api/course/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"test\",\"price\":100.0}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test405_對course_id_送PUT 通過");
    }

    @Test
    public void test405_對category_id_送PUT() throws Exception {
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
    public void test405_對category_id_送PATCH() throws Exception {
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
    public void test415_POST沒帶ContentType() throws Exception {
        mockMvc.perform(post("/api/course")
                        .content("{\"courseName\":\"測試\",\"price\":100.0}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ test415_POST沒帶ContentType 通過");
    }

    @Test
    public void test415_ContentType為純文字() throws Exception {
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
    public void test500_未預期例外() throws Exception {
        when(courseService.findById(42L))
                .thenThrow(new RuntimeException("Something bad happened"));

        mockMvc.perform(get("/api/course/42"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something bad happened"))
                .andExpect(jsonPath("$.timestamp").exists());

        System.out.println("✅ test500_未預期例外 通過");
    }

    @Test
    public void test500_findAll拋出例外() throws Exception {
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
    public void testErrorResponse_三個欄位都存在() throws Exception {
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
    public void testErrorResponse_details包含uri資訊() throws Exception {
        when(courseService.findById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Course not found: 7"));

        mockMvc.perform(get("/api/course/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("/api/course/7")));

        System.out.println("✅ testErrorResponse_details包含uri資訊 通過");
    }

    @Test
    public void testGetAll_正常回傳() throws Exception {
        when(courseService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/course/all"))
                .andExpect(status().isOk());

        System.out.println("✅ testGetAll_正常回傳 通過");
    }
}