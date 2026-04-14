package com.ctbc.assignment2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 這是 Spring Boot 預設產生的測試類別。
 * 用來驗證整體的 Spring 應用程式上下文 (Application Context) 是否能夠順利啟動。
 * 對於初學者來說，這是檢查設定檔有沒有寫錯、依賴注入有沒有設定壞掉的第一道防線。
 */
@SpringBootTest
class Assignment2ApplicationTests {

	@Test
	void contextLoads() {
        // 如果 Spring 容器無法啟動（例如 Bean 互相循環依賴、設定錯誤等），
        // 這裡即使裡面沒有程式碼，這個測試也會失敗 (Failed)。
	}

}
