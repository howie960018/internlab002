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

// @WebMvcTest 負責啟動 Controller 層環境，並註冊指定的 Web 控制器，不會完整載入整個 Spring (沒包含 Service 跟 DB 層)
@WebMvcTest(controllers = {
        CourseWebController.class,
        CategoryWebController.class,
        HomeWebController.class
})
// 導入我們想要測試的例外處理器（因為它可能有 @ControllerAdvice 但在這裡預設沒被掃到）
@Import(WebExceptionHandler.class)
public class WebExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc; 

    // 使用 @MockBean 在應用上下文中構造一個替身 (虛擬服務) 來替代原本的實體，只專注於測試 Controller 收到例外時的反應
    @MockBean
    private CourseBeanService courseService;

    @MockBean
    private CourseCategoryBeanService categoryService;

    // ════════════════════════════════════════════════════
    //   ResourceNotFoundException → error view
    // ════════════════════════════════════════════════════

    @Test
        public void testWeb404CourseNotFoundToErrorView() throws Exception {
        when(courseService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Course not found: 99999"));

        mockMvc.perform(get("/course/edit/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "Course not found: 99999"));

        System.out.println("✅ testWeb404_課程不存在_導到error頁 通過");
    }

    @Test
        public void testWeb404CategoryNotFoundToErrorView() throws Exception {
        when(categoryService.findById(99999L))
                .thenThrow(new ResourceNotFoundException("Category not found: 99999"));

        mockMvc.perform(get("/category/edit/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "Category not found: 99999"));

        System.out.println("✅ testWeb404_類別不存在_導到error頁 通過");
    }

    @Test
        public void testWeb404DeleteMissingCourseToErrorView() throws Exception {
        doThrow(new ResourceNotFoundException("Course not found: 99999"))
                .when(courseService).deleteById(99999L);

        mockMvc.perform(get("/course/delete/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "Course not found: 99999"));

        System.out.println("✅ testWeb404_刪除不存在的課程_導到error頁 通過");
    }

    @Test
        public void testWeb404DeleteMissingCategoryToErrorView() throws Exception {
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
    //   【重要說明】
    //   CourseWebController 和 CategoryWebController 的 save() 方法
    //   自己 catch DuplicateCourseNameException，回到表單頁顯示錯誤訊息，
    //   「不會」拋給 WebExceptionHandler 處理。
    //   因此正確的測試預期：view = "course/form"，model 有 "duplicateError"。
    // ════════════════════════════════════════════════════

    @Test
        public void testWeb409DuplicateCourseNameStayOnForm() throws Exception {
        when(courseService.save(any()))
                .thenThrow(new DuplicateCourseNameException("課程名稱已存在：Java 基礎"));
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                        .param("courseName", "Java 基礎")
                        .param("price", "3000.0"))
                .andExpect(status().isOk())
                // 【修正】Controller catch 後 return "course/form"，不走 WebExceptionHandler
                .andExpect(view().name("course/form"))
                .andExpect(model().attributeExists("duplicateError"));

        System.out.println("✅ testWeb409_新增重複課程名稱_留在表單頁顯示錯誤 通過");
    }

    @Test
        public void testWeb409DuplicateCategoryNameStayOnForm() throws Exception {
        when(categoryService.save(any()))
                .thenThrow(new DuplicateCourseNameException("類別名稱已存在：Java"));

        mockMvc.perform(post("/category/save")
                        .param("categoryName", "Java"))
                .andExpect(status().isOk())
                // 【修正】Controller catch 後 return "category/form"，不走 WebExceptionHandler
                .andExpect(view().name("category/form"))
                .andExpect(model().attributeExists("duplicateError"));

        System.out.println("✅ testWeb409_新增重複類別名稱_留在表單頁顯示錯誤 通過");
    }

    // ════════════════════════════════════════════════════
    //   DataIntegrityViolationException → error view
    //   （Controller 沒有 catch DataIntegrityViolationException，會傳到 WebExceptionHandler）
    // ════════════════════════════════════════════════════

    @Test
        public void testWeb409DbConstraintViolationToErrorView() throws Exception {
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
        public void testWeb409CourseDbConstraintViolationToErrorView() throws Exception {
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
    //   MethodArgumentTypeMismatchException → error view
    // ════════════════════════════════════════════════════

    @Test
        public void testWeb400PathVariableTypeMismatchToErrorView() throws Exception {
        mockMvc.perform(get("/course/edit/abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));

        System.out.println("✅ testWeb400_PathVariable型態不符_導到error頁 通過");
    }

    @Test
        public void testWeb400CategoryPathVariableTypeMismatchToErrorView() throws Exception {
        mockMvc.perform(get("/category/edit/xyz"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));

        System.out.println("✅ testWeb400_類別PathVariable型態不符_導到error頁 通過");
    }

    @Test
        public void testWeb400DeletePathVariableTypeMismatchToErrorView() throws Exception {
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
        public void testWebValidationFailCourseNameBlankStayOnForm() throws Exception {
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/course/save")
                        .param("courseName", "")
                        .param("price", "100.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("course/form"));

        System.out.println("✅ testWeb_表單驗証失敗_課程名稱空白_留在表單頁 通過");
    }

    @Test
        public void testWebValidationFailCategoryNameBlankStayOnForm() throws Exception {
        mockMvc.perform(post("/category/save")
                        .param("categoryName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("category/form"));

        System.out.println("✅ testWeb_表單驗証失敗_類別名稱空白_留在表單頁 通過");
    }

    @Test
        public void testWebValidationFailNegativePriceStayOnForm() throws Exception {
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
        public void testWeb500UnexpectedErrorToErrorView() throws Exception {
        when(courseService.findPage(any()))
                .thenThrow(new RuntimeException("資料庫連線失敗"));

        mockMvc.perform(get("/course/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "系統發生未預期錯誤：資料庫連線失敗"));

        System.out.println("✅ testWeb500_未預期例外_導到error頁 通過");
    }

    @Test
        public void testWeb500CategoryUnexpectedErrorToErrorView() throws Exception {
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
        public void testHomeLoadsSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));

        System.out.println("✅ testHome_正常顯示首頁 通過");
    }
}