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


@RestControllerAdvice(basePackages = "com.ctbc.assignment2.controller.rest")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ── 404：找不到資源 ──────────────────────────────────────────
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
