package com.ctbc.assignment2.exception;

import java.util.Date;

// 自定義錯誤回應物件：用於封裝 API 發生例外時的回傳資訊
public class ErrorResponse {
    private Date timestamp;
    private String message;
    private String details;

    public ErrorResponse(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public Date getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public String getDetails() { return details; }
}
