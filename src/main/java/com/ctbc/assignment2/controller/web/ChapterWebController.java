package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.Chapter;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.service.ChapterService;
import com.ctbc.assignment2.service.CourseBeanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/course/{courseId}")
public class ChapterWebController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private CourseBeanService courseService;

    @GetMapping("/chapters")
    public String list(@PathVariable Long courseId, Model model) {
        List<Chapter> chapters = chapterService.findByCourse(courseId);
        CourseBean course = courseService.findById(courseId);
        model.addAttribute("chapters", chapters);
        model.addAttribute("courseId", courseId);
        model.addAttribute("course", course);
        model.addAttribute("courseName", course.getCourseName());
        return "admin/chapter/list";
    }

    @GetMapping("/chapter/form")
    public String showForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("chapter", new Chapter());
        model.addAttribute("courseId", courseId);
        return "admin/chapter/form";
    }

    @GetMapping("/chapter/edit/{id}")
    public String edit(@PathVariable Long courseId, @PathVariable Long id, Model model) {
        model.addAttribute("chapter", chapterService.findById(id));
        model.addAttribute("courseId", courseId);
        return "admin/chapter/form";
    }

    @PostMapping("/chapter/save")
    public String save(@PathVariable Long courseId,
                       @Valid Chapter chapter,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", courseId);
            return "admin/chapter/form";
        }
        chapter.setCourse(courseService.findById(courseId));
        chapterService.save(chapter);
        return "redirect:/admin/course/" + courseId + "/chapters";
    }

    @PostMapping("/chapter/delete/{id}")
    public String delete(@PathVariable Long courseId, @PathVariable Long id) {
        chapterService.delete(id);
        return "redirect:/admin/course/" + courseId + "/chapters";
    }
}
