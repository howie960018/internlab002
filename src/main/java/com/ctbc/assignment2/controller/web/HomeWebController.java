package com.ctbc.assignment2.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 網頁首頁的控制器 (Web Controller)
 * 用於處理一般網頁瀏覽 (View) 的請求，例如當使用者進入首頁時，回傳對應的 HTML 頁面給瀏覽器繪製。
 * 這是跟 REST 控制器不同之處 (REST通常是回傳 JSON 格式)。
 */
@Controller
public class HomeWebController {

    /**
     * 當使用者瀏覽網址根目錄 ("/") 或 "/home" 時，觸發這個方法。
     * 
     * @return 返回值字串 "home" 代表請 Spring MVC 到資源目錄尋找名叫 "home.html" 的模板頁面，並渲染呈現出來。
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }
}
