package com.ctbc.assignment2.controller.rest;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

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
    public CourseBean getById(@PathVariable Long id) {
        return courseService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        courseService.deleteById(id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public void unsupportedIdMethods(org.springframework.http.HttpMethod method) throws org.springframework.web.HttpRequestMethodNotSupportedException {
        throw new org.springframework.web.HttpRequestMethodNotSupportedException(method.name());
    }

    @PostMapping
    public CourseBean save(@Valid @RequestBody CourseBean course) {
        return courseService.save(course);
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
