package com.ctbc.assignment2.controller.rest;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 課程分類 REST 控制器 (REST Controller)
 * 負責處理前端透過 /api/category 發送過來的 HTTP 請求 (例如 GET, POST, DELETE 等)。
 * @RestController 代表裡面所有方法的傳回值，都會自動轉成 JSON 格式回傳給前端。
 */
@RestController
@RequestMapping("/api/category")
@CrossOrigin // 允許跨來源資源共用(Cross-Origin Resource Sharing)
public class CategoryBeanRestController {

    // 透過 Spring 自動注入(DI) 類別的業務邏輯服務層(Service)
    @Autowired
    private CourseCategoryBeanService categoryService;

    /**
     * 處理 GET 請求 : /api/category/all
     * 回傳所有現存的課程分類資料清單。
     */
    @GetMapping("/all")
    public List<CourseCategoryBean> getAll() {
        return categoryService.findAll();
    }

    /**
     * 處理 GET 請求 : /api/category/{id}
     * 根據從 URL 網址傳入的分類 ID，回傳對應的那一筆分類資料。
     * @PathVariable 讓 Spring 幫我們將 URL 路徑裡面的變數 (也就是 {id})，放到我們的方法參數中。
     */
    @GetMapping("/{id}")
    public CourseCategoryBean getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    /**
     * 處理 DELETE 請求 : /api/category/{id}
     * 根據傳入的分類 ID，將該分類刪除。
     */
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        categoryService.deleteById(id);
    }

    /**
     * 如果使用者透過 PUT 或 PATCH 的方法試圖修改特定 id (例如： PUT /api/category/1) ，因為本系統尚不支援，所以丟出例外 (Exception)
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public void unsupportedIdMethods(org.springframework.http.HttpMethod method) throws org.springframework.web.HttpRequestMethodNotSupportedException {
        throw new org.springframework.web.HttpRequestMethodNotSupportedException(method.name());
    }

    /**
     * 處理 POST 請求 : /api/category
     * 新增一筆課程分類資料。
     * @RequestBody 會自動將前端傳送過來的 JSON 內容，轉換為 Java 的 CourseCategoryBean 物件。
     * @Valid 會根據在 Bean 裡設定的驗證規則(如 @NotBlank) 檢查前端帶過來的資料是否符合規定。
     */
    @PostMapping
    public CourseCategoryBean save(@Valid @RequestBody CourseCategoryBean category) {
        return categoryService.save(category);
    }
}
