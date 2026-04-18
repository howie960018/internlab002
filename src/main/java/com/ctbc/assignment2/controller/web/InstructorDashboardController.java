package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseStatus;
import com.ctbc.assignment2.exception.DuplicateCourseNameException;
import com.ctbc.assignment2.exception.InvalidFileException;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import com.ctbc.assignment2.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/instructor")
public class InstructorDashboardController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        List<CourseBean> courses = courseService.findByInstructorName(principal.getName());
        long publishedCount = courses.stream()
                .filter(course -> course.getStatus() == CourseStatus.PUBLISHED)
                .count();
        model.addAttribute("publishedCount", publishedCount);
        return "instructor/dashboard";
    }

    @GetMapping("/courses")
    public String list(Principal principal, Model model) {
        List<CourseBean> courses = courseService.findByInstructorName(principal.getName());
        model.addAttribute("courses", courses);
        return "instructor/course/list";
    }

    @GetMapping("/course/form")
    public String showForm(Principal principal, Model model) {
        CourseBean course = new CourseBean();
        course.setInstructorName(principal.getName());
        model.addAttribute("course", course);
        model.addAttribute("categories", categoryService.findAll());
        return "instructor/course/form";
    }

    @PostMapping("/course/save")
    public String save(@Valid @org.springframework.web.bind.annotation.ModelAttribute("course") CourseBean course,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) MultipartFile coverImage,
                       Principal principal,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "instructor/course/form";
        }
        try {
            if (categoryId != null) {
                course.setCategory(categoryService.findById(categoryId));
            }
            course.setInstructorName(principal.getName());
            course.setStatus(CourseStatus.DRAFT);

            if (coverImage != null && !coverImage.isEmpty()) {
                String url = fileStorageService.store(coverImage);
                course.setCoverImageUrl(url);
            }

            courseService.save(course);
        } catch (DuplicateCourseNameException e) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("duplicateError", e.getMessage());
            return "instructor/course/form";
        } catch (InvalidFileException e) {
            bindingResult.rejectValue("coverImageUrl", "file.invalid", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "instructor/course/form";
        }
        return "redirect:/instructor/courses";
    }
}
