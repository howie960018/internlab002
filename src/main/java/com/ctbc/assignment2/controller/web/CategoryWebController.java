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

import java.util.UUID;

/**
 * 課程分類專用的網頁控制器 (Web Controller)
 * 主要用以處理與課程分類介面操作相關的邏輯 (負責導向到 Thymeleaf 的層級 HTML 畫面)。
 */
@Controller
@RequestMapping("/category")
public class CategoryWebController {

    // 透過 Spring 自動注入(DI) 所需的課程分類服務層 (Service)
    @Autowired
    private CourseCategoryBeanService categoryService;

    /**
     * 讀取所有分類的列表，然後導向到顯示清單的 HTML 頁面。
     * @param model 將資料夾帶給前端 Thymeleaf 引擎作渲染用
     */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "category/list";
    }

    /**
     * 導向分類的建立表單頁面。
     * 建立一個空的 CourseCategoryBean 物件，提供給前端表單綁定輸入欄位。
     */
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("category", new CourseCategoryBean());
        return "category/form";
    }

    /**
     * 負責接收並儲存前端表單回傳的類別資料 (新增或修改)。
     * 若資料有誤 (如空白)，會直接將錯誤帶回原表單頁面提示。
     * 
     * @param category 這裡對應到前端表單 `th:object="${category}"` 綁定的資料內容
     * @param bindingResult 用來捕捉 @Valid 驗證欄位的錯誤(必須直接放在被驗證的參數後方)
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") CourseCategoryBean category,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            // 【修正】@ModelAttribute("category") 確保 Spring 自動將物件放入 model，
            // 讓 Thymeleaf th:object="${category}" 發生錯誤時能正確將原填寫數值和錯誤訊息繪製出來而不會清空
            return "category/form";
        }
        try {
            // 寫進資料庫
            categoryService.save(category);
        } catch (DuplicateCourseNameException e) {
            // 例外捕捉，如果是名稱重複
            model.addAttribute("duplicateError", e.getMessage());
            return "category/form";
        }
        // 新增成功後跳轉回列表頁面
        return "redirect:/category/list";
    }

    /**
     * 導向編輯分類的頁面。
     * 點下清單的編輯按鈕時，此方法會利用 id 從資料庫查出現存的類別然後塞進表單中供使用者編輯。
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "category/form";
    }

    /**
     * 刪除特定的分類。
     * @param id 取出網址上的 id，告訴服務層 (Service) 把這筆分類給砍了。
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable UUID id) {
        categoryService.deleteById(id);
        return "redirect:/category/list";
    }
}