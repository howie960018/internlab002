package com.ctbc.assignment2.exception;

import org.springframework.core.annotation.Order;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 全域例外處理器 (專門處理 REST API 發生的錯誤)
 * 🌟 @RestControllerAdvice: 代表它會攔截指定套件內的 Controller 錯誤，並自動將回傳結果轉換為 JSON 格式。
 * 這裡設定只攔截 "com.ctbc.assignment2.controller.rest" 底下的 API 請求。
 */
@RestControllerAdvice(basePackages = "com.ctbc.assignment2.controller.rest")
// @RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 處理 404 找不到資源的錯誤
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 處理 409 自訂的名稱重複錯誤
    @ExceptionHandler(DuplicateCourseNameException.class)
    public ResponseEntity<Object> handleDuplicateCourseName(
            DuplicateCourseNameException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 處理 409 資料庫層級的限制違反 (例如外鍵衝突等)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(
            DataIntegrityViolationException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "資料違反資料庫限制，請確認輸入內容", request);
    }

    

    // 處理 400 網址參數型態錯誤 (例如預期要 UUID，卻傳了普通字串)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String msg = String.format("參數 '%s' 的值 '%s' 型態不正確，預期型態為 %s",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName() : "未知");
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    // 處理 400 實體驗證失敗 (例如 Entity 裡的 @NotBlank, @NotNull 沒通過)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // 將多個欄位的錯誤訊息串接起來
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("、"));
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    // 處理 400 前端傳來的 JSON 格式根本無法解析時
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "JSON 格式錯誤：" + ex.getMessage(), request);
    }

    // 處理 405 請求方法錯誤 (例如該用 POST 卻用 GET 呼叫)
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "不支援的 HTTP 方法：" + ex.getMethod(), request);
    }

    // 處理 415 內容類型不支援 (例如沒加上 Content-Type: application/json)
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "不支援的 Content-Type：" + ex.getContentType(), request);
    }

    // 兜底處理：處理所有上面沒有攔截到的未知例外，統一回傳 500 內部伺服器錯誤
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // 🌟 共用的小工具：負責把錯誤資訊打包成 ErrorResponse 物件
    private ResponseEntity<Object> build(
            HttpStatus status, String message, WebRequest request) {
        
        // request.getDescription(false) 會取得發生錯誤的網址路徑，放進 details 欄位裡
        ErrorResponse error = new ErrorResponse(
                new Date(), message, request.getDescription(false));
        
        return new ResponseEntity<>(error, status);
    }
}