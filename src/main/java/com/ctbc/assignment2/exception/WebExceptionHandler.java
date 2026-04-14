package com.ctbc.assignment2.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

// 針對 web 頁面的 Controller
@ControllerAdvice(basePackages = "com.ctbc.assignment2.controller.web")
public class WebExceptionHandler {

    // ── 找不到資源（404） ─────────────────────────────────────────
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

    // ── DB constraint 違反（409） ─────────────────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(
            DataIntegrityViolationException ex, Model model) {
        model.addAttribute("errorTitle", "資料庫限制違反");
        model.addAttribute("errorMessage", "資料違反資料庫限制，請確認輸入內容");
        return "error";
    }

    // ── Path Variable 型態不符（400） ─────────────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("errorTitle", "網址格式錯誤");
        model.addAttribute("errorMessage",
            "網址參數格式錯誤：「" + ex.getName() + "」應為數字，實際收到「" + ex.getValue() + "」");
        return "error";
    }

    // ── Bean Validation 失敗兜底（400） ──────────────────────────
    // 當 @Valid @ModelAttribute 沒有搭配 BindingResult 時觸發
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, Model model) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> fieldLabel(e.getField()) + "：" + e.getDefaultMessage())
            .collect(Collectors.toList());
        model.addAttribute("errorTitle", "輸入驗證失敗");
        model.addAttribute("errorMessage", "請修正以下欄位錯誤：");
        model.addAttribute("fieldErrors", errors);
        return "error";
    }

    // ── 未預期例外（500） ─────────────────────────────────────────
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
