package com.ctbc.assignment2.controller.rest;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

// @RestController: 標示這個類別是 RESTful 控制器，方法的返回值會自動轉成 JSON 格式回傳給客戶端（等同於 @Controller + @ResponseBody 的組合）
// @RequestMapping("/api/course"): 定義這個控制器內所有 API 的共用 URL 前綴
// @CrossOrigin: 允許跨來源資源共用 (CORS)，方便前端從其他不同網域或 Port 來存取此 API
@RestController
@RequestMapping("/api/course")
@CrossOrigin
public class CourseBeanRestController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    // @GetMapping: 處理 HTTP GET 請求（通常用來查詢資料），路徑結合上面設定的變為 /api/course/all
    @GetMapping("/all")
    public List<CourseBean> getAll() {
        return courseService.findAll();
    }

    // @GetMapping("/{id}"): 路徑中包含一個稱為 {id} 的動態變數(佔位符)
    // @PathVariable: 指示 Spring 從 URL 路徑中擷取這個 {id} 值，並綁定到方法的參數 Long id 身上
    @GetMapping("/{id}")
    public CourseBean getById(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        courseService.deleteById(id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public void unsupportedIdMethods(org.springframework.http.HttpMethod method) throws org.springframework.web.HttpRequestMethodNotSupportedException {
    // @PostMapping: 處理 HTTP POST 請求（通常用於新增資料）
    // @RequestBody: 將 HTTP 請求的本體(Request Body中的 JSON 資料) 自動轉換為 Java 物件(CourseBean)
    // @Valid: 觸發資料驗證，驗證規則須寫在 CourseBean 內部的欄位上 (例如 @NotBlank, @Min... 等)
        throw new org.springframework.web.HttpRequestMethodNotSupportedException(method.name());
    }

    @PostMapping
    public CourseBean save(@Valid @RequestBody CourseBean course) {
        return courseService.save(course);
    }

    @PostMapping("/batch")
    public List<CourseBean> saveBatch(@Valid @RequestBody List<CourseBean> courses) {
        return courseService.saveBatch(courses);
    }

    @PostMapping("/category/{categoryId}")
    public CourseBean saveWithCategory(
            @Valid @RequestBody CourseBean course,
            @PathVariable Long categoryId) {
        CourseCategoryBean category = categoryService.findById(categoryId);
        course.setCategory(category);
        return courseService.save(course);
    }
}
