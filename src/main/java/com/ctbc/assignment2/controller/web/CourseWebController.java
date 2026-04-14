package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

/**
 * 課程專用的網頁控制器 (Web Controller)
 * 主要用以處理與課程介面操作相關的邏輯 (負責導向到 Thymeleaf 的 HTML 畫面)。
 */
@Controller
@RequestMapping("/course")
public class CourseWebController {

    // 透過 Spring 自動注入(DI) 所需的服务層 (Service)
    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    /**
     * 取得課程清單的頁面。
     * @param model 將資料(Model)傳到 Thymeleaf 的 HTML 中的幫手
     * @return 返回 Thymeleaf 的 HTML 範本名稱 (在這裡對應到 resources/templates/course/list.html)
     */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "course/list";
    }

    /**
     * 開啟建立/編輯課程。
     * 會產生一個新的 CourseBean 物件且讀取所有的課程分類放進 Model 中。
     * @return 前往前台填寫表單頁面的名稱 "course/form"
     */
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("course", new CourseBean());
        model.addAttribute("categories", categoryService.findAll());
        return "course/form";
    }

    /**
     * 保存(新增或更新) 使用者提交過來的表單資料 (發送 POST 請求送來)。
     * 
     * @param course 由前端綁定填寫內容的物件
     * @param bindingResult 檢查前端送過來的 course 是否有違反 @Valid 驗證規則，收集錯誤的物件。必須緊接在 @Valid 後面
     * @param categoryId 這門課對應到的分類的ID
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("course") CourseBean course,
                       BindingResult bindingResult,
                       @RequestParam(required = false) UUID categoryId,
                       Model model) {
        // 如果傳來的資料不符合規則(例如名稱空白之類)，這裡 BindingResult 就會抓到
        if (bindingResult.hasErrors()) {
            // 【修正】BindingResult 有錯時，Thymeleaf th:object="${course}" 需要 model 中有 course
            // 使用 @ModelAttribute("course") 後 Spring 會自動放入，但仍補上 categories 避免 HTML 取不到下拉選單 (會爆炸)
            model.addAttribute("categories", categoryService.findAll());
            return "course/form"; // 退回表單讓使用者重填
        }
        try {
            // 如果這門課程要加上分類
            if (categoryId != null) {
                course.setCategory(categoryService.findById(categoryId));
            }
            // 寫入資料庫
            courseService.save(course);
        } catch (DuplicateCourseNameException e) {
            // 如果拋出自訂例外 (捕捉到已存在的重複名課程)
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("duplicateError", e.getMessage());
            return "course/form"; // 退回表單讓使用者重新填寫
        }
        // 如果沒問題，導向(重新觸發)獲取此物件清單的行為(重整表單最新狀態的意思)
        return "redirect:/course/list";
    }

    /**
     * 編輯單一筆現存的課程資料。
     * 點擊編輯按鈕時，透過傳入的課程 id 抓出資料庫內的課程物件放到 model 給頁面用。
     * @param id 課程編號
     * @return 前往前台填寫表單的名稱 "course/form"
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "course/form";
    }

    /**
     * 根據傳入的 ID 將該筆課程資料執行刪除。
     * 完成刪除後，跳轉回課程清單列表頁面。
     * @param id 欲刪除的課程 ID
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable UUID id) {
        courseService.deleteById(id);
        return "redirect:/course/list";
    }
}