package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/category")
public class CategoryWebController {

    @Autowired
    private CourseCategoryBeanService categoryService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "category/list";
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("category", new CourseCategoryBean());
        return "category/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") CourseCategoryBean category,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            // 【修正】@ModelAttribute("category") 確保 Spring 自動將物件放入 model，
            // Thymeleaf th:object="${category}" 才能正確渲染
            return "category/form";
        }
        try {
            categoryService.save(category);
        } catch (DuplicateCourseNameException e) {
            model.addAttribute("duplicateError", e.getMessage());
            return "category/form";
        }
        return "redirect:/category/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "category/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/category/list";
    }
}