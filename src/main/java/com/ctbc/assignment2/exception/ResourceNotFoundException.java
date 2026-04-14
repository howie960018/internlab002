package com.ctbc.assignment2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 自訂例外：當透過 ID (或其他條件) 嘗試去資料庫尋找一筆資料，卻找不到的時候拋出此例外。
 * @ResponseStatus(HttpStatus.NOT_FOUND) 代表這個例外發生時，Spring 預設會回傳 404 Not Found 的 HTTP 狀態碼。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 建構子：接收自訂錯誤訊息並往父類別 (RuntimeException) 傳遞。
     * @param message 發生例外發生時要顯示的錯誤訊息 (例如 "找不到 ID 為 1 的課程")
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
