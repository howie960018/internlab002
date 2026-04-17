package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PublicCourseWebController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @GetMapping("/courses")
    public String browse(@RequestParam(required = false) Long id,
                         @RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "9") int size,
                         Model model) {
        model.addAttribute("categories", buildCategoryTree());

        String keyword = q != null ? q.trim() : null;
        Page<CourseBean> pageResult;

        if (id != null) {
            CourseCategoryBean selected = categoryService.findById(id);
            List<Long> categoryIds = new ArrayList<>();
            categoryIds.add(id);
            List<CourseCategoryBean> children = categoryService.findChildren(id);
            for (CourseCategoryBean child : children) {
                categoryIds.add(child.getId());
            }

            if (keyword != null && !keyword.isBlank()) {
                pageResult = courseService.findPageByCategoryIdsAndName(categoryIds, keyword, PageRequest.of(page, size));
            } else {
                pageResult = courseService.findPageByCategoryIds(categoryIds, PageRequest.of(page, size));
            }

            model.addAttribute("selectedCategory", selected);
            model.addAttribute("selectedCategoryId", id);
        } else {
            if (keyword != null && !keyword.isBlank()) {
                pageResult = courseService.findPageByName(keyword, PageRequest.of(page, size));
            } else {
                pageResult = courseService.findPage(PageRequest.of(page, size));
            }
        }

        model.addAttribute("query", keyword);
        model.addAttribute("courses", pageResult.getContent());
        model.addAttribute("currentPage", pageResult.getNumber());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("pageSize", pageResult.getSize());
        return "courses/index";
    }

    @GetMapping("/courses/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.findById(id));
        return "courses/detail";
    }

    private List<CategoryNode> buildCategoryTree() {
        List<CategoryNode> nodes = new ArrayList<>();
        List<CourseCategoryBean> topLevel = categoryService.findTopLevel();
        for (CourseCategoryBean parent : topLevel) {
            List<CourseCategoryBean> children = categoryService.findChildren(parent.getId());
            nodes.add(new CategoryNode(parent, children));
        }
        return nodes;
    }

    private static class CategoryNode {
        private final CourseCategoryBean category;
        private final List<CourseCategoryBean> children;

        private CategoryNode(CourseCategoryBean category, List<CourseCategoryBean> children) {
            this.category = category;
            this.children = children;
        }

        public CourseCategoryBean getCategory() { return category; }
        public List<CourseCategoryBean> getChildren() { return children; }
    }
}
