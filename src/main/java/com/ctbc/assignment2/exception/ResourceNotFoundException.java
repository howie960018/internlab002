package com.ctbc.assignment2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus 用來指定當此例外被拋出時回傳的 HTTP 狀態碼
// HttpStatus.NOT_FOUND 代表 404 找不到資源
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException { // 自定義例外，用來處理「找不到資料」的情境
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
