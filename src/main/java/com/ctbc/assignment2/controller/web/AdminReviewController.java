package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.service.CourseReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminReviewController {

    @Autowired
    private CourseReviewService reviewService;

    @GetMapping("/course/{courseId}/reviews")
    public String list(@PathVariable Long courseId, Model model) {
        model.addAttribute("reviews", reviewService.findByCourse(courseId));
        model.addAttribute("courseId", courseId);
        return "admin/review/list";
    }

    @PostMapping("/review/delete/{id}")
    public String delete(@PathVariable Long id, @RequestParam Long courseId) {
        reviewService.delete(id);
        return "redirect:/admin/course/" + courseId + "/reviews";
    }
}
