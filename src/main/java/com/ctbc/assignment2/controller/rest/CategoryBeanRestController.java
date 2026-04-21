package com.ctbc.assignment2.controller.rest;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;


/**
 * 課程分類 REST 控制器 (REST Controller)
 * 負責處理前端透過 /api/category 發送過來的 HTTP 請求 (例如 GET, POST, DELETE 等)。
 * @RestController 代表裡面所有方法的傳回值，都會自動轉成 JSON 格式回傳給前端。
 */
@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryBeanRestController {

    @Autowired
    private CourseCategoryBeanService categoryService;

    @GetMapping("/all")
    public List<CourseCategoryBean> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public CourseCategoryBean getById(@PathVariable UUID id) {
        return categoryService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        categoryService.deleteById(id);
    }

    @PutMapping("/{id}")
    public CourseCategoryBean updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CourseCategoryBean category) {
        category.setId(id);
        return categoryService.save(category);
    }

    @PostMapping
    public CourseCategoryBean createCategory(
            @Valid @RequestBody CourseCategoryBean category) {
        return categoryService.save(category);
    }
}