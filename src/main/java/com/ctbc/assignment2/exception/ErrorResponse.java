package com.ctbc.assignment2.exception;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 錯誤包裝盒 (Data Transfer Object)
 * 用來封裝「當發生例外時，要統一回傳給前端的 JSON 錯誤格式」的物件。
 * 確保前端每次拿到的錯誤格式都是固定的，方便在畫面上顯示提示。
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    
    // 發生錯誤的當下時間
    private Date timestamp;
    
    // 錯誤的簡短描述 (例如："找不到資源"、"課程名稱已存在")
    private String message;
    
    // 錯誤的詳細資訊 (例如："uri=/api/courses/1" 或是其他輔助說明的細節)
    // 這裡命名為 details 比 path 更有彈性，未來如果想塞入其他類型的詳細資訊也很適合
    private String details;  
}