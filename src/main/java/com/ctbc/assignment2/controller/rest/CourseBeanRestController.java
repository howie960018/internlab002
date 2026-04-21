package com.ctbc.assignment2.controller.rest;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;


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

    @GetMapping("/all")
    public List<CourseBean> getAll() {
        return courseService.findAll();
    }

    @GetMapping("/{id}")
    public CourseBean getById(@PathVariable UUID id) {
        return courseService.findById(id);
    }

    @PostMapping
    public CourseBean save(@Valid @RequestBody CourseBean course) {
        return courseService.save(course);
    }

    @PostMapping("/category/{categoryId}")
    public CourseBean saveWithCategory(
            @Valid @RequestBody CourseBean course,
            @PathVariable UUID categoryId) {
        CourseCategoryBean category = categoryService.findById(categoryId);
        course.setCategory(category);
        return courseService.save(course);
    }

    @PutMapping("/{id}")
    public CourseBean updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseBean course) {
        course.setId(id);
        return courseService.save(course);
    }

    @PatchMapping("/{id}")
    public CourseBean patchCourse(
            @PathVariable UUID id,
            @RequestBody CourseBean patchData) {
        CourseBean existing = courseService.findById(id);
        if (patchData.getCourseName() != null) {
            existing.setCourseName(patchData.getCourseName());
        }
        if (patchData.getPrice() != null) {
            existing.setPrice(patchData.getPrice());
        }
        return courseService.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        courseService.deleteById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<CourseBean> getCoursesByCategory(@PathVariable UUID categoryId) {
        return courseService.findByCategoryId(categoryId);
    }
}
