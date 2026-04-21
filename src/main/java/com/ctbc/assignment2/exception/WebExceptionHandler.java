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
 * 針對這專案中的網頁型控制器 (Web Controller) 的全域例外處理器。
 * 此類別使用了 @ControllerAdvice，會攔截發生在 "com.ctbc.assignment2.controller.web" 的例外。
 * 被攔截後，不同於 API 會回傳 JSON，這裡我們是要將錯誤訊息放入 Model 中，
 * 然後導向到自訂的 Thymeleaf 錯誤頁面 "error.html" 呈現給使用網頁的用戶看。
 */

@ControllerAdvice(basePackages = "com.ctbc.assignment2.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(
            ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "找不到資源");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(DuplicateCourseNameException.class)
    public String handleDuplicateCourseName(
            DuplicateCourseNameException ex, Model model) {
        model.addAttribute("errorTitle", "名稱重複");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(
            DataIntegrityViolationException ex, Model model) {
        model.addAttribute("errorTitle", "資料庫限制違反");
        model.addAttribute("errorMessage", "資料違反資料庫限制，請確認輸入內容");
        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("errorTitle", "網址格式錯誤");
        model.addAttribute("errorMessage",
                "網址參數格式錯誤：「" + ex.getName()
                + "」應為數字，實際收到「" + ex.getValue() + "」");
        return "error";
    }

    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, Model model) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(e -> fieldLabel(e.getField()) + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());
        model.addAttribute("errorTitle", "輸入驗證失敗");
        model.addAttribute("errorMessage", "請修正以下欄位錯誤：");
        model.addAttribute("fieldErrors", errors);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        model.addAttribute("errorTitle", "系統錯誤");
        model.addAttribute("errorMessage", "系統發生未預期錯誤：" + ex.getMessage());
        return "error";
    }

    private String fieldLabel(String field) {
        return switch (field) {
            case "courseName"   -> "課程名稱";
            case "price"        -> "價格";
            case "categoryName" -> "類別名稱";
            default             -> field;
        };
    }
}