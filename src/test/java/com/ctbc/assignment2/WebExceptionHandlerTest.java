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

/**
 * 專門負責測試我們的【前端畫面 (Thymeleaf) 錯誤處理器 (WebExceptionHandler)】是否有發揮作用。
 * 和 API 錯誤處理不同：網頁發生錯誤時，我們不是要把 JSON 回傳給使用者，
 * 而是要「導向一個美美的錯誤頁面 (error.html)」，把錯誤訊息放在畫面裡給使用者看。
 * 
 * 所以你可以看到這裡的 Assert 都是寫 .andExpect(view().name("error")) 或是 .andExpect(model().XXX)
 */
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

    // ════════════════════════════════════════════════════
    //   ResourceNotFoundException → 導向 error 頁面
    // ════════════════════════════════════════════════════

    @Test
    public void testWeb404_課程不存在_導到error頁() throws Exception {
        // 設定假人
        when(courseService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Course not found: 99999"));

        // 去敲擊網頁修改畫面
        mockMvc.perform(get("/course/edit/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error")) // 預期它會把我們導去 error.html
                .andExpect(model().attribute("errorMessage", "Course not found: 99999")); // 預期會把錯誤文字帶到畫面的 errorMessage 中

        System.out.println("✅ testWeb404_課程不存在_導到error頁 通過");
    }

    @Test
    public void testWeb404_類別不存在_導到error頁() throws Exception {
        when(categoryService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Category not found: 99999"));

        mockMvc.perform(get("/category/edit/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "Category not found: 99999"));

        System.out.println("✅ testWeb404_類別不存在_導到error頁 通過");
    }

    @Test
    public void testWeb404_刪除不存在的課程_導到error頁() throws Exception {
        doThrow(new ResourceNotFoundException("Course not found: 99999"))
                .when(courseService).deleteById(99999L);

        mockMvc.perform(get("/course/delete/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "Course not found: 99999"));

        System.out.println("✅ testWeb404_刪除不存在的課程_導到error頁 通過");
    }

    @Test
    public void testWeb404_刪除不存在的類別_導到error頁() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found: 99999"))
                .when(categoryService).deleteById(99999L);

        mockMvc.perform(get("/category/delete/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "Category not found: 99999"));

        System.out.println("✅ testWeb404_刪除不存在的類別_導到error頁 通過");
    }

    // ════════════════════════════════════════════════════
    //   DuplicateCourseNameException → 留在表單頁顯示 duplicateError
    //
    //   【初學者常見問題 - 重要說明】
    //   為甚麼重複名稱的時候不是跳轉去 error.html？
    //   因為這是我們寫前端畫面的重要體驗：如果使用者輸入名稱錯誤，直接跳到別的畫面會很錯愕，
    //   所以通常「輸入表單時的情境」，Controller 會自己把 Exception 攔截，然後再次回傳原來的 form.html！
    // ════════════════════════════════════════════════════

    @Test
    public void testWeb409_新增重複課程名稱_留在表單頁顯示錯誤() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DuplicateCourseNameException("課程名稱已存在：Java 基礎"));
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                        .param("courseName", "Java 基礎")
                        .param("price", "3000.0"))
                .andExpect(status().isOk())
                // 【測試重點】Controller catch 後 return "course/form"，不經過 WebExceptionHandler 的導向
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("duplicateError")); // 會往前端送出錯誤提示文字

        System.out.println("✅ testWeb409_新增重複課程名稱_留在表單頁顯示錯誤 通過");
    }

    @Test
    public void testWeb409_新增重複類別名稱_留在表單頁顯示錯誤() throws Exception {
        when(categoryService.save(any()))
                .thenThrow(new DuplicateCourseNameException("類別名稱已存在：Java"));

        mockMvc.perform(post("/category/save")
                        .param("categoryName", "Java"))
                .andExpect(status().isOk())
                // 【測試重點】回到原表單
                .andExpect(view().name("category/form"))
                .andExpect(model().attributeExists("duplicateError"));

        System.out.println("✅ testWeb409_新增重複類別名稱_留在表單頁顯示錯誤 通過");
    }

    // ════════════════════════════════════════════════════
    //   DataIntegrityViolationException → 導向 error 頁面
    //   (當底層拋出意外的完整性錯誤時)
    // ════════════════════════════════════════════════════

    @Test
    public void testWeb409_DB_constraint違反_導到error頁() throws Exception {
        when(categoryService.save(any()))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));

        mockMvc.perform(post("/category/save")
                        .param("categoryName", "重複類別"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "資料違反資料庫限制，請確認輸入內容"));

        System.out.println("✅ testWeb409_DB_constraint違反_導到error頁 通過");
    }

    @Test
    public void testWeb409_課程DB_constraint違反_導到error頁() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                        .param("courseName", "測試課程")
                        .param("price", "100.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "資料違反資料庫限制，請確認輸入內容"));

        System.out.println("✅ testWeb409_課程DB_constraint違反_導到error頁 通過");
    }

    // ════════════════════════════════════════════════════
    //   MethodArgumentTypeMismatchException → 導向 error 頁面
    // ════════════════════════════════════════════════════

    @Test
    public void testWeb400_PathVariable型態不符_導到error頁() throws Exception {
        // ID 應該是數字，但我偏偏送出文字 abc
        mockMvc.perform(get("/course/edit/abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));

        System.out.println("✅ testWeb400_PathVariable型態不符_導到error頁 通過");
    }

    @Test
    public void testWeb400_類別PathVariable型態不符_導到error頁() throws Exception {
        mockMvc.perform(get("/category/edit/xyz"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));

        System.out.println("✅ testWeb400_類別PathVariable型態不符_導到error頁 通過");
    }

    @Test
    public void testWeb400_刪除時PathVariable型態不符_導到error頁() throws Exception {
        mockMvc.perform(get("/course/delete/notANumber"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));

        System.out.println("✅ testWeb400_刪除時PathVariable型態不符_導到error頁 通過");
    }

    // ════════════════════════════════════════════════════
    //   Bean Validation (BindingResult) → 留在表單頁
    // ════════════════════════════════════════════════════

    @Test
    public void testWeb_表單驗証失敗_課程名稱空白_留在表單頁() throws Exception {
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                        .param("courseName", "")
                        .param("price", "100.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"));

        System.out.println("✅ testWeb_表單驗証失敗_課程名稱空白_留在表單頁 通過");
    }

    @Test
    public void testWeb_表單驗証失敗_類別名稱空白_留在表單頁() throws Exception {
        mockMvc.perform(post("/category/save")
                        .param("categoryName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"));

        System.out.println("✅ testWeb_表單驗証失敗_類別名稱空白_留在表單頁 通過");
    }

    @Test
    public void testWeb_表單驗証失敗_價格為負數_留在表單頁() throws Exception {
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                        .param("courseName", "測試課程")
                        .param("price", "-1.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"));

        System.out.println("✅ testWeb_表單驗証失敗_價格為負數_留在表單頁 通過");
    }

    // ════════════════════════════════════════════════════
    //   Exception catch-all → error view
    // ════════════════════════════════════════════════════

    @Test
    public void testWeb500_未預期例外_導到error頁() throws Exception {
        when(courseService.findAll())
                .thenThrow(new RuntimeException("資料庫連線失敗"));

        mockMvc.perform(get("/course/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "系統發生未預期錯誤：資料庫連線失敗"));

        System.out.println("✅ testWeb500_未預期例外_導到error頁 通過");
    }

    @Test
    public void testWeb500_類別查詢未預期例外_導到error頁() throws Exception {
        when(categoryService.findAll())
                .thenThrow(new RuntimeException("NullPointerException"));

        mockMvc.perform(get("/category/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "系統發生未預期錯誤：NullPointerException"));

        System.out.println("✅ testWeb500_類別查詢未預期例外_導到error頁 通過");
    }

    // ════════════════════════════════════════════════════
    //   首頁正常載入
    // ════════════════════════════════════════════════════

    @Test
    public void testHome_正常顯示首頁() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));

        System.out.println("✅ testHome_正常顯示首頁 通過");
    }
}