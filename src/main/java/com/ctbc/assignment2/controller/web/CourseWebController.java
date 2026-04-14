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

@Controller
@RequestMapping("/course")
public class CourseWebController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("courses", courseService.findAll());
        return "course/list";
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("course", new CourseBean());
        model.addAttribute("categories", categoryService.findAll());
        return "course/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("course") CourseBean course,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Long categoryId,
                       Model model) {
        if (bindingResult.hasErrors()) {
            // 【修正】BindingResult 有錯時，Thymeleaf th:object="${course}" 需要 model 中有 course
            // 使用 @ModelAttribute("course") 後 Spring 會自動放入，但仍補上 categories 避免 NPE
            model.addAttribute("categories", categoryService.findAll());
            return "course/form";
        }
        try {
            if (categoryId != null) {
                course.setCategory(categoryService.findById(categoryId));
            }
            courseService.save(course);
        } catch (DuplicateCourseNameException e) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("duplicateError", e.getMessage());
            return "course/form";
        }
        return "redirect:/course/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "course/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return "redirect:/course/list";
    }
}