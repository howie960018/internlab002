package com.ctbc.assignment2.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 【網頁畫面的全域例外處理器】
 * 
 * `@ControllerAdvice`: 這個標籤就像是整個 Web Controller 的守門員（或攔截器）。
 * 只要是在 `basePackages` 指定的包 (例如 `controller.web`) 底下發生的錯誤，
 * 都會被統一攔截到這裡處理。
 * 與 `@RestControllerAdvice` 不同，這裡處理完後會回傳一個「字串 (String)」，
 * 這個字串代表的是「視圖名稱」(View Name)（例如: "error" 代表 error.html），用來導向錯誤頁面，而不是直接回傳 JSON。
 */
@ControllerAdvice(basePackages = "com.ctbc.assignment2.controller.web")
public class WebExceptionHandler {

    // ── 找不到資源（404） ─────────────────────────────────────────
    /**
     * `@ExceptionHandler`: 告訴 Spring 當發生括號內的 Exception 時，請交給這個方法處理。
     * 例如這裡專門抓取 `ResourceNotFoundException`。
     * 
     * @param ex 被捕捉到的例外物件
     * @param model 用來傳遞資料給 Thymeleaf 網頁畫面的物件
     * @return 導向的 Thymeleaf 模板名稱（會對應到 src/main/resources/templates/error.html）
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(
            ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "找不到資源");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // ── 重複名稱（409） ───────────────────────────────────────────
    @ExceptionHandler(DuplicateCourseNameException.class)
    public String handleDuplicateCourseName(
            DuplicateCourseNameException ex, Model model) {
        model.addAttribute("errorTitle", "名稱重複");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(CategoryNotEmptyException.class)
    public String handleCategoryNotEmpty(
            CategoryNotEmptyException ex, Model model) {
        model.addAttribute("errorTitle", "類別不可刪除");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(CategoryHierarchyException.class)
    public String handleCategoryHierarchy(
            CategoryHierarchyException ex, Model model) {
        model.addAttribute("errorTitle", "類別層級錯誤");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // ── DB constraint 違反（409） ─────────────────────────────────
    /**
     * 當資料庫層級發生限制衝突（例如：儲存了違反 Unique 約束的資料，或是 Nullable 限制）
     * 會丟出 DataIntegrityViolationException。我們在這裡攔截它並給出友善提示。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(
            DataIntegrityViolationException ex, Model model) {
        model.addAttribute("errorTitle", "資料庫限制違反");
        model.addAttribute("errorMessage", "資料違反資料庫限制，請確認輸入內容");
        return "error";
    }

    // ── Path Variable 型態不符（400） ─────────────────────────────
    /**
     * 當網址上的參數型別不符合預期。
     * 例如：@PathVariable 期待一個 Long (數字) `/courses/1`，但使用者輸入字串 `/courses/abc` 時會觸發。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("errorTitle", "網址格式錯誤");
        model.addAttribute("errorMessage",
            "網址參數格式錯誤：「" + ex.getName() + "」應為數字，實際收到「" + ex.getValue() + "」");
        return "error";
    }

    // ── Bean Validation 失敗兜底（400） ──────────────────────────
    /**
     * 當表單送出的資料沒有通過實體類別（例如 CourseBean）上的 `@NotBlank` 或 `@NotNull` 驗證，
     * 且所在的 Controller 沒有寫 `BindingResult` 來接錯時，就會丟出 `BindException`。
     */
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, Model model) {
        // 從例外中取出所有沒通過驗證的欄位錯誤，組合成好讀的清單
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> fieldLabel(e.getField()) + "：" + e.getDefaultMessage())
            .collect(Collectors.toList());
            
        model.addAttribute("errorTitle", "輸入驗證失敗");
        model.addAttribute("errorMessage", "請修正以下欄位錯誤：");
        model.addAttribute("fieldErrors", errors); // 將所有錯誤清單傳給畫面顯示
        return "error";
    }

    // ── 未預期例外（500） ─────────────────────────────────────────
    /**
     * Exception.class 是所有例外的老大哥（基底類別）。
     * 如果前面都沒有對應的 ExceptionHandler 抓到錯誤，最後就會落入這個「兜底」的處理器。
     */
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        model.addAttribute("errorTitle", "系統錯誤");
        model.addAttribute("errorMessage", "系統發生未預期錯誤：" + ex.getMessage());
        return "error";
    }

    // ── 欄位名稱轉中文 ────────────────────────────────────────────
    private String fieldLabel(String field) {
        return switch (field) {
            case "courseName"   -> "課程名稱";
            case "price"        -> "價格";
            case "categoryName" -> "類別名稱";
            default             -> field;
        };
    }
}
