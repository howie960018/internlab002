package com.ctbc.assignment2.exception;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
/**
 * 網頁專用的全域例外處理器
 * 🌟 @ControllerAdvice: 不同於 API 會回傳 JSON，這裡是用來攔截 "com.ctbc.assignment2.controller.web" 的錯誤。
 * 它的任務是將錯誤訊息塞進 Model 中，並導向我們寫好的 "error.html" 錯誤頁面，讓一般使用者能看到友善的畫面。
 */
// @ControllerAdvice(basePackages = "com.ctbc.assignment2.controller.web")
// @ControllerAdvice(annotations = Controller.class)



@ControllerAdvice(basePackages = "com.ctbc.assignment2.controller.web")
public class WebExceptionHandler {

    // 處理找不到資源的錯誤
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(
            ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "找不到資源");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error"; // 🌟 導向 error.html
    }

    // 處理名稱重複的錯誤
    @ExceptionHandler(DuplicateCourseNameException.class)
    public String handleDuplicateCourseName(
            DuplicateCourseNameException ex, Model model) {
        model.addAttribute("errorTitle", "名稱重複");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // 處理資料庫限制違反
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(
            DataIntegrityViolationException ex, Model model) {
        model.addAttribute("errorTitle", "資料庫限制違反");
        model.addAttribute("errorMessage", "資料違反資料庫限制，請確認輸入內容");
        return "error";
    }

    // 處理網址參數格式錯誤
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("errorTitle", "網址格式錯誤");
        model.addAttribute("errorMessage",
                "網址參數格式錯誤：「" + ex.getName()
                + "」應為數字，實際收到「" + ex.getValue() + "」");
        return "error";
    }

    // 處理表單驗證失敗 (例如網頁送出表單時，欄位沒填寫)
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, Model model) {
        // 將錯誤欄位轉換為我們看得懂的中文標籤
        List<String> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(e -> fieldLabel(e.getField()) + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());
        model.addAttribute("errorTitle", "輸入驗證失敗");
        model.addAttribute("errorMessage", "請修正以下欄位錯誤：");
        model.addAttribute("fieldErrors", errors); // 將多個欄位錯誤清單傳給畫面
        return "error";
    }

    // 兜底處理：未知的系統錯誤
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        model.addAttribute("errorTitle", "系統錯誤");
        model.addAttribute("errorMessage", "系統發生未預期錯誤：" + ex.getMessage());
        return "error";
    }

    // 🌟 小工具：把英文的屬性名稱轉換成中文，讓錯誤訊息對使用者更友善
    private String fieldLabel(String field) {
        return switch (field) {
            case "courseName"   -> "課程名稱";
            case "price"        -> "價格";
            case "categoryName" -> "類別名稱";
            default             -> field;
        };
    }
}