package com.ctbc.assignment2.exception;

import java.util.Date;

/**
 * 用來封裝「當發生例外時，要統一回傳給前端的 JSON 錯誤格式」的物件。
 * 通常用於 REST API 發生錯誤時，將雜亂的 Exception 轉換為結構化的 JSON 內容。
 */
public class ErrorResponse {
    
    // 發生錯誤的時間點
    private Date timestamp;
    
    // 發生錯誤的簡短文字訊息 (例如: 找不到對應的 ID)
    private String message;
    
    // 發生錯誤的詳細說明 (可能是前端傳來的網址路徑或更細節的錯誤原因)
    private String details;

    /**
     * 建構子，用來在發生例外時快速生成這個錯誤回應物件
     */
    public ErrorResponse(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    // ================= 以下為 Getter (讓 Spring 能夠讀取這些屬性並轉成 JSON) =================

    public Date getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public String getDetails() { return details; }
}
