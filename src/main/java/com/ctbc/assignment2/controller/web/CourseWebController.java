package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

/**
 * 課程專用的網頁控制器 (Web Controller)
 * 主要用以處理與課程介面操作相關的邏輯 (負責導向到 Thymeleaf 的 HTML 畫面)。
 */
@Controller
@RequestMapping("/course")
public class CourseWebController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) UUID categoryId, Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        if (categoryId != null) {
            model.addAttribute("courses", courseService.findByCategoryId(categoryId));
        } else {
            model.addAttribute("courses", courseService.findAll());
        }
        return "course/list";
    }
    
    //改進版
//    @GetMapping("/list")
//    public String list(@RequestParam(required = false) UUID categoryId, Model model) {
//        model.addAttribute("categories", categoryService.findAll());
//        
//        if (categoryId != null) {
//            try {
//                model.addAttribute("courses", courseService.findByCategoryId(categoryId));
//                model.addAttribute("selectedCategoryId", categoryId);
//            } catch (ResourceNotFoundException e) {
//                // 分類已被刪除，回到全部清單
//                return "redirect:/course/list";
//            }
//        } else {
//            model.addAttribute("courses", courseService.findAll());
//        }
//        return "course/list";
//    }

    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("course", new CourseBean());
        model.addAttribute("categories", categoryService.findAll());
        return "course/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("course") CourseBean course,
                       BindingResult bindingResult,
                       @RequestParam(required = false) UUID categoryId,
                       Model model) {
        if (bindingResult.hasErrors()) {
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
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "course/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable UUID id) {
        courseService.deleteById(id);
        return "redirect:/course/list";
    }
}