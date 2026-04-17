package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Spring MVC 課程網頁控制器 (Web Controller)
 * 負責處理與「課程」相關之前端請求與畫面切換。
 */
@Controller // 標記為 MVC Controller (會掃描成 Bean 並返回 View 頁面名稱)
@RequestMapping("/admin") // 請求的共同前綴都會是: /admin
public class CourseWebController {

    // 依賴注入 (DI)：將 CourseBeanService 等由 Spring 自動實例注入
    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    /**
     * @GetMapping 表示對應 HTTP 的 GET 請求
     * Model: 提供給 View 顯示的前端資料容器
     */
    @GetMapping("/courses")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<CourseBean> pageResult = courseService.findPage(PageRequest.of(page, size));
        // addAttribute(Key, Value) 可以在 Thymeleaf 以 ${courses} 取得其內容
        model.addAttribute("courses", pageResult.getContent());
        model.addAttribute("currentPage", pageResult.getNumber());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("pageSize", pageResult.getSize());
        return "admin/course/list";
    }

    /**
     * 跳轉到新增課程的表單
     */
    @GetMapping("/course/form")
    public String showForm(Model model) {
        model.addAttribute("course", new CourseBean()); // 初始化空物件給表單，用於欄位綁定
        model.addAttribute("categories", categoryService.findAll()); // 準備類別選單資料
        return "admin/course/form";
    }

    /**
     * 儲存課程 (新增或修改) -> POST 請求通常用於送出、新增資料
     * @Valid: 執行 Bean Validation 檢查
     * @ModelAttribute("course"): 自動封裝前端傳入表單對應欄位，如果有錯誤會回顯示在畫面上
     * @RequestParam: 根據表單的 name 屬性抓取特定欄位的值 (通常用來接外鍵 ID / 隱藏欄位)，required = false 允許為 null
     */
    @PostMapping("/course/save")
    public String save(@Valid @ModelAttribute("course") CourseBean course,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Long categoryId,
                       Model model) {
        if (bindingResult.hasErrors()) {
            // 【修正】BindingResult 有錯時，Thymeleaf th:object="${course}" 需要 model 中有 course
            // 使用 @ModelAttribute("course") 後 Spring 會自動放入，但仍補上 categories 避免 NPE
            model.addAttribute("categories", categoryService.findAll());
            return "admin/course/form";
        }
        try {
            if (categoryId != null) {
                // 如果有選類別，把類別實體從 Database 撈出來後塞入這筆課程的物件中建立關聯
                course.setCategory(categoryService.findById(categoryId));
            }
            courseService.save(course);
        } catch (DuplicateCourseNameException e) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("duplicateError", e.getMessage());
            return "admin/course/form";
        }
        // redirect 表示這不是個檔案路徑，而是告訴瀏覽器重新要求 URL 路徑
        return "redirect:/admin/courses";
    }

    /**
     * 用於編輯，將網址列傳入的 id 抓出來 (@PathVariable)，查詢資料庫帶入表單顯示
     */
    @GetMapping("/course/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "admin/course/form"; // 前端使用同一支 form.html 來處理新增與修改
    }

    @PostMapping("/course/delete/{id}")
    public String delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/courses";
    }

    @PostMapping("/course/deleteBatch")
    public String deleteBatch(@RequestParam(name = "ids", required = false) java.util.List<Long> ids) {
        courseService.deleteBatch(ids);
        return "redirect:/admin/courses";
    }
}