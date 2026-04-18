package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import com.ctbc.assignment2.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @Autowired(required = false)
    private EnrollmentService enrollmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("courseCount", courseService.count());
        model.addAttribute("categoryCount", categoryService.count());
        if (enrollmentService != null) {
            List<CourseBean> courses = courseService.findAll();
            Map<Long, Long> enrollmentCounts = new HashMap<>();
            for (CourseBean course : courses) {
                enrollmentCounts.put(course.getId(), enrollmentService.countByCourse(course.getId()));
            }
            model.addAttribute("enrollmentCounts", enrollmentCounts);
        }
        return "admin/dashboard";
    }
}
