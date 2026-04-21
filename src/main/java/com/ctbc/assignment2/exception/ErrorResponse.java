package com.ctbc.assignment2.exception;

import java.util.Date;

/**
 * 用來封裝「當發生例外時，要統一回傳給前端的 JSON 錯誤格式」的物件。
 * 通常用於 REST API 發生錯誤時，將雜亂的 Exception 轉換為結構化的 JSON 內容。
 */
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ErrorResponse {
	private Date timestamp;
	private String message;
	private String details;  
}