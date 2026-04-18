package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.Enrollment;
import com.ctbc.assignment2.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/my-courses")
public class MyCourseWebController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping
    public String list(Principal principal, Model model) {
        List<Enrollment> all = enrollmentService.findByStudent(principal.getName());
        List<Enrollment> active = new ArrayList<>();
        for (Enrollment enrollment : all) {
            if ("ACTIVE".equals(enrollment.getStatus())) {
                active.add(enrollment);
            }
        }
        model.addAttribute("enrollments", active);
        return "my-courses/index";
    }

    @PostMapping("/cancel/{courseId}")
    public String cancel(@PathVariable Long courseId, Principal principal) {
        enrollmentService.cancel(principal.getName(), courseId);
        return "redirect:/my-courses";
    }
}
