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
 * 全域例外處理器 (專門處理 @RestController 層級發生的錯誤)
 * 此類別使用了 @RestControllerAdvice，代表它會扮演一個「攔截(攔查)」的角色。
 * 只要是在 "com.ctbc.assignment2.controller.rest" 裡面的控制器有拋出例外 (Exception)，
 * 就會自動被導到這裡來統一整理後變成 JSON 回覆給前端，幫助我們有條理的處理 HTTP 錯誤狀態碼。
 */
@RestControllerAdvice(basePackages = "com.ctbc.assignment2.controller.rest")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ── 404：找不到資源 (當拋出我們自訂的 ResourceNotFoundException 時攔截到這裡並回傳) ──────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ── 409：重複課程名稱 (當拋出我們自訂的 DuplicateCourseNameException 時攔截到這裡並回傳) ────────────────────────────────────────
    @ExceptionHandler(DuplicateCourseNameException.class)
    public ResponseEntity<Object> handleDuplicateCourseName(
            DuplicateCourseNameException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ── 409：DB constraint 違反（兜底） ─────────────────────────
    // 當資料庫限制條件被觸發 (像是外鍵約束衝突)，Spring Data JPA 會拋出 DataIntegrityViolationException
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(
            DataIntegrityViolationException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "資料違反資料庫限制，請確認輸入內容", request);
    }

    // ── 400：Path Variable 型態不符 ────────────────────────
    // 當網址列帶的參數與我們所規範的屬性不合 (如 /api/course/abc 但期望接收 Long)，父類別未處理這例外，直接加 @ExceptionHandler
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String msg = String.format("參數 '%s' 的值 '%s' 型態不正確，預期型態為 %s",
            ex.getName(),
            ex.getValue(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知");
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    // ── 400：Bean Validation 失敗（@Valid 檢查物件內容未過關時） ─────────
    // 這方法在父類別 (ResponseEntityExceptionHandler) 已處理，因此必須 @Override 用我們自訂的方法處理回傳邏輯
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + "：" + e.getDefaultMessage())
            .collect(Collectors.joining("、"));
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    // ── 400：JSON 格式錯誤 (輸入之 JSON 無法轉成 Java RequestBody 時) ───────────────────────────────────────
    // 父類別已處理 → 必須 @Override，不能再加 @ExceptionHandler
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "JSON 格式錯誤：" + ex.getMessage(), request);
    }

    // ── 405：HTTP 方法不支援 ─────────────────────────────────────
    // 例如我們只有實作 GET，但前端用了 PUT 請求，就會攔截到這。父類別已處理 → 必須 @Override
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
            "不支援的 HTTP 方法：" + ex.getMethod(), request);
    }

    // ── 415：Content-Type 不支援 ─────────────────────────────────
    // 前端送錯 Content-Type 格式 (如 application/xml 而非 application/json)。父類別已處理 → 必須 @Override
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "不支援的 Content-Type：" + ex.getContentType(), request);
    }

    // ── 500：未預期例外（catch-all） ─────────────────────────────
    // 當發生了任何上面我們都沒列出的「未預期 Exception」時，最後會掉到這。
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // ── 共用 builder ─────────────────────────────────────────────
    /**
     * Helper 協助方法，能產生把 ErrorResponse 包裝成符合標準的 ResponseEntity
     * 讓上面的錯誤處理區塊程式碼更精簡
     */
    private ResponseEntity<Object> build(
            HttpStatus status, String message, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
            new Date(), message, request.getDescription(false)
        );
        return new ResponseEntity<>(error, status);
    }
}
