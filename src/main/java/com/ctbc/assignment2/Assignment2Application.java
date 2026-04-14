package com.ctbc.assignment2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 應用程式入口點 (Entry Point)
 * 
 * - @SpringBootApplication: 是一個「組合」標註，等同於三個重要的標籤集合：
 *   1. @Configuration: 讓這個類別可以做為 Spring 配置檔的撰寫處 (定義額外 Bean)。
 *   2. @EnableAutoConfiguration: Spring Boot 強大的魔法來源，它會依據引用專案的依賴自動設置、設定需要的元件 (如連線池, Hibernate 等)。
 *   3. @ComponentScan: 告訴 Spring 只要在這個 Package 以及其子 Package 底下所有帶有 @Component, @Service, @Repository, @Controller 等標籤的類別，就自動轉成元件並納入 Spring 管理。
 */
@SpringBootApplication
public class Assignment2Application {

	/**
	 * 主程式 (Main Method): 
	 * 當我們啟動專案時，Java 呼叫此 main 方法，接著透過 SpringApplication.run() 開始載入整個 Spring 環境，
	 * 初始化那些在 @Service, @RestController 等標籤宣告的 Bean。
	 */
	public static void main(String[] args) {
		SpringApplication.run(Assignment2Application.class, args);
	}

}
