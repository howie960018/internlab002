package com.ctbc.assignment2.controller.rest;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

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
    public CourseCategoryBean getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        categoryService.deleteById(id);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public void unsupportedIdMethods(org.springframework.http.HttpMethod method) throws org.springframework.web.HttpRequestMethodNotSupportedException {
        throw new org.springframework.web.HttpRequestMethodNotSupportedException(method.name());
    }

    @PostMapping
    public CourseCategoryBean save(@Valid @RequestBody CourseCategoryBean category) {
        return categoryService.save(category);
    }
}
