package com.ctbc.assignment2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus 用來指定當此例外被拋出時，API 應回傳的 HTTP 狀態碼
// HttpStatus.CONFLICT 代表 409 衝突，適合用在資源已存在的狀況（如：名稱重複）
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCourseNameException extends RuntimeException { // 繼承 RuntimeException，屬於自定義的執行時期例外
    public DuplicateCourseNameException(String message) {
        super(message);
    }
}
