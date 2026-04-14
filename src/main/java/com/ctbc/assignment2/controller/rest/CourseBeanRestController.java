package com.ctbc.assignment2.controller.rest;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;


/**
 * 課程 REST 控制器，提供課程相關的 API 介面。
 */
@RestController
@RequestMapping("/api/course")
@CrossOrigin
public class CourseBeanRestController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    /**
     * 取得所有課程清單。
     * 
     * @return 包含所有課程的 List
     */
    @GetMapping("/all")
    public List<CourseBean> getAll() {
        return courseService.findAll();
    }

    /**
     * 根據課程 ID 取得特定課程。
     * 
     * @param id 課程 ID
     * @return 符合 ID 的課程物件
     */
    @GetMapping("/{id}")
    public CourseBean getById(@PathVariable java.util.UUID id) {
        return courseService.findById(id);
    }

    /**
     * 根據課程 ID 刪除特定課程。
     * 
     * @param id 要刪除的課程 ID
     */
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable java.util.UUID id) {
        courseService.deleteById(id);
    }

    /**
     * 處理不支援的 HTTP 請求方法（PUT, PATCH）。
     * 當使用者對 /{id} 路徑發送 PUT 或 PATCH 請求時，會拋出例外。
     * 
     * @param method 請求的 HTTP 方法
     * @throws org.springframework.web.HttpRequestMethodNotSupportedException 不支援的請求方法例外
     */
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public void unsupportedIdMethods(org.springframework.http.HttpMethod method) throws org.springframework.web.HttpRequestMethodNotSupportedException {
        throw new org.springframework.web.HttpRequestMethodNotSupportedException(method.name());
    }

    /**
     * 儲存新的課程資料。
     * 
     * @param course 要新增的課程物件，必須通過驗證
     * @return 儲存成功後的課程物件
     */
    @PostMapping
    public CourseBean save(@Valid @RequestBody CourseBean course) {
        return courseService.save(course);
    }

    /**
     * 儲存具有關聯類別的課程資料。
     * 
     * @param course 要新增的課程物件，必須通過驗證
     * @param categoryId 課程所屬類別的 ID
     * @return 儲存成功後的課程物件（包含類別資訊）
     */
    @PostMapping("/category/{categoryId}")
    public CourseBean saveWithCategory(
            @Valid @RequestBody CourseBean course,
            @PathVariable java.util.UUID categoryId) {
        // 先根據分類 ID 尋找並載入分類物件
        CourseCategoryBean category = categoryService.findById(categoryId);
        // 設定課程的關聯分類
        course.setCategory(category);
        // 儲存課程物件
        return courseService.save(course);
    }
}
