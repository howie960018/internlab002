package com.ctbc.assignment2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 自訂例外：當嘗試新增或修改的課程/分類名稱，在資料庫中已經存在時拋出此例外。
 * @ResponseStatus(HttpStatus.CONFLICT) 代表這個例外發生時，Spring 預設會回傳 409 Conflict (資源衝突) 的 HTTP 狀態碼。
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCourseNameException extends RuntimeException {
    
    /**
     * 建構子：接收自訂錯誤訊息並往父類別 (RuntimeException) 傳遞。
     * @param message 發生例外發生時要顯示的錯誤訊息 (例如 "此課程名稱已存在")
     */
    public DuplicateCourseNameException(String message) {
        super(message);
    }
}
