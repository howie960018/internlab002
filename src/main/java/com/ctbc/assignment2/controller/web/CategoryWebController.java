package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Spring MVC 控制器 (Web Controller) 負責管理「課程類別」
 * 負責接收來自用戶端瀏覽器的請求 (Request)，交由 Service 層處理商業邏輯後，再回傳適當的視圖 (View, 例如 Thymeleaf 的 HTML)
 */
@Controller // @Controller 代表此類別為前端介面回傳網頁的控制器
@RequestMapping("/category") // 所有路徑都統一以 "/category" 開頭
public class CategoryWebController {

    // @Autowired: 依賴注入機制 (Dependency Injection)。讓 Spring 自動尋找符合這個介面或類別的元件並裝載進來 (不用手動 new)
    @Autowired
    private CourseCategoryBeanService categoryService;

    /**
     * @GetMapping 列出所有類別的方法
     * Model: 一個用來傳遞資料給前端頁面 (View) 的容器
     */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "category/list"; // 回傳 resources/templates/category/list.html 畫面的路徑
    }

    /**
     * 顯示新增表單的方法
     * "category" 模型放一個空物件，供前端表單榜定欄位
     */
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("category", new CourseCategoryBean());
        return "category/form";
    }

    /**
     * @PostMapping: 負責接收表單送出 (提交) 的 HTTP POST 請求
     * @Valid: 告訴 Spring 要對這個表單資料作實體驗證 (@NotBlank, @NotNull 等限制)
     * @ModelAttribute: 把前端來的表單資料自動綁定到 CourseCategoryBean 物件內 ("category")
     * BindingResult: 用來接 @Valid 驗證失敗的錯誤結果
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") CourseCategoryBean category,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            // 【修正】@ModelAttribute("category") 確保 Spring 自動將物件放入 model，
            // Thymeleaf th:object="${category}" 才能正確渲染
            return "category/form";
        }
        try {
            categoryService.save(category);
        } catch (DuplicateCourseNameException e) {
            model.addAttribute("duplicateError", e.getMessage());
            return "category/form";
        }
        // "redirect:" 告訴瀏覽器重新導向到指定的 URL (不是畫面檔，是 controller 路徑)
        return "redirect:/category/list";
    }

    /**
     * @PathVariable: 抓取 URL 路徑大括弧 {id} 的動態變數內容作為方法的參數參數
     * 常用於獲取特定 ID 的資料來作修改動作
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "category/form";
    }

    /**
     * 利用 id 進行刪除
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/category/list";
    }
}