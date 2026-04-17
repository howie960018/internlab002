package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("courseCount", courseService.findAll().size());
        model.addAttribute("categoryCount", categoryService.findAll().size());
        return "admin/dashboard";
    }
}
