package com.ctbc.assignment2.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.Date;
import java.util.stream.Collectors;


/**
 * 全域例外處理 (Global Exception Handler)
 * 
 * - @RestControllerAdvice: 這是一個切面 (AOP) 工具。加上這個標籤，代表這是一個全域級別的錯誤攔截器。
 *   只要 basePackages 底下的 Controller 發生例外，且沒被 try-catch 攔住，就會往外拋到這裡處理。
 *   由於它是 @ControllerAdvice 和 @ResponseBody 的組合，因此回傳值會自動被轉換為 JSON 格式 (符合 RESTful API 習慣)。
 * - ResponseEntityExceptionHandler: Spring 提供的實用基礎類別。裡面已經寫好了處理一些標準 Spring MVC Exception 的模板，我們可以重寫(override)部分方法即可。
 */
@RestControllerAdvice(basePackages = "com.ctbc.assignment2.controller.rest")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ── 404：找不到資源 ──────────────────────────────────────────
    /**
     * @ExceptionHandler 標籤用來指定「當拋出何種 Exception 時，這支方法會負責接手處理」。
     * 以這裡為例，當程式拋出 ResourceNotFoundException 時，會執行下方這行，回傳 404 及對應錯誤訊息給前端。
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ── 409：重複課程名稱 ────────────────────────────────────────
    @ExceptionHandler(DuplicateCourseNameException.class)
    public ResponseEntity<Object> handleDuplicateCourseName(
            DuplicateCourseNameException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ── 409：類別不可刪除 ───────────────────────────────────────
    @ExceptionHandler(CategoryNotEmptyException.class)
    public ResponseEntity<Object> handleCategoryNotEmpty(
            CategoryNotEmptyException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ── 409：類別層級錯誤 ───────────────────────────────────────
    @ExceptionHandler(CategoryHierarchyException.class)
    public ResponseEntity<Object> handleCategoryHierarchy(
            CategoryHierarchyException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ── 409：重複報名 ──────────────────────────────────────────
    @ExceptionHandler(DuplicateEnrollmentException.class)
    public ResponseEntity<Object> handleDuplicateEnrollment(
            DuplicateEnrollmentException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ── 400：檔案格式錯誤 ─────────────────────────────────────
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Object> handleInvalidFile(
            InvalidFileException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ── 409：DB constraint 違反（兜底） ─────────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(
            DataIntegrityViolationException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "資料違反資料庫限制，請確認輸入內容", request);
    }

    // ── 400：Path Variable 型態不符（如 /api/course/abc） ────────
    // 父類別未處理，直接加 @ExceptionHandler
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String msg = String.format("參數 '%s' 的值 '%s' 型態不正確，預期型態為 %s",
            ex.getName(),
            ex.getValue(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知");
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    // ── 400：Bean Validation 失敗（@Valid @RequestBody） ─────────
    // 父類別已處理 → 必須 @Override
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + "：" + e.getDefaultMessage())
            .collect(Collectors.joining("、"));
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    // ── 400：JSON 格式錯誤 ───────────────────────────────────────
    // 父類別已處理 → 必須 @Override，不能加 @ExceptionHandler
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "JSON 格式錯誤：" + ex.getMessage(), request);
    }

    // ── 405：HTTP 方法不支援 ─────────────────────────────────────
    // 父類別已處理 → 必須 @Override
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
            "不支援的 HTTP 方法：" + ex.getMethod(), request);
    }

    // ── 415：Content-Type 不支援 ─────────────────────────────────
    // 父類別已處理 → 必須 @Override
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "不支援的 Content-Type：" + ex.getContentType(), request);
    }

    // ── 500：未預期例外（catch-all） ─────────────────────────────
    /**
     * 預設處理所有未分類的 Exception
     * 
     * 如果發生的錯誤沒有上述任何一個對應的 @ExceptionHandler 攔截，就會跌入這個最大的兜底防線 (Exception.class)。
     * 這能確保就算程式出錯，前端也能收到格式一致 (JSON)、狀態碼合理的錯誤回覆 (如 HTTP 500 Server Error)。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // ── 共用 builder ─────────────────────────────────────────────
    private ResponseEntity<Object> build(
            HttpStatus status, String message, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            new Date(), message, request.getDescription(false)
        );
        return new ResponseEntity<>(error, status);
    }
}
