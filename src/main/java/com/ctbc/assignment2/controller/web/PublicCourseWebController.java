package com.ctbc.assignment2.controller.web;

import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseCategoryBean;
import com.ctbc.assignment2.bean.CourseStatus;
import com.ctbc.assignment2.exception.DuplicateEnrollmentException;
import com.ctbc.assignment2.repository.CourseReviewRepository;
import com.ctbc.assignment2.repository.EnrollmentRepository;
import com.ctbc.assignment2.service.ChapterService;
import com.ctbc.assignment2.service.CourseBeanService;
import com.ctbc.assignment2.service.CourseCategoryBeanService;
import com.ctbc.assignment2.service.CourseReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PublicCourseWebController {

    @Autowired
    private CourseBeanService courseService;

    @Autowired
    private CourseCategoryBeanService categoryService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private CourseReviewService reviewService;

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @GetMapping("/courses")
    public String browse(@RequestParam(required = false) Long id,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) Double minPrice,
                         @RequestParam(required = false) Double maxPrice,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "9") int size,
                         Model model) {
        model.addAttribute("categories", buildCategoryTree());

        String keyword = q != null ? q.trim() : null;
        Page<CourseBean> pageResult = courseService.search(
                keyword,
                id,
                CourseStatus.PUBLISHED,
                minPrice,
                maxPrice,
                PageRequest.of(page, size));

        if (id != null) {
            CourseCategoryBean selected = categoryService.findById(id);
            model.addAttribute("selectedCategory", selected);
            model.addAttribute("selectedCategoryId", id);
        }

        model.addAttribute("query", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("courses", pageResult.getContent());
        model.addAttribute("currentPage", pageResult.getNumber());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalElements", pageResult.getTotalElements());
        model.addAttribute("pageSize", pageResult.getSize());
        return "courses/index";
    }

    @GetMapping("/courses/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        model.addAttribute("course", courseService.findById(id));
        model.addAttribute("chapters", chapterService.findByCourse(id));

        List<com.ctbc.assignment2.bean.CourseReview> reviews = reviewService.findByCourse(id);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", reviewService.getAvgRating(id));
        model.addAttribute("reviewCount", reviews.size());

        boolean alreadyReviewed = principal != null
                && reviewRepository.existsByCourseIdAndReviewerUsername(id, principal.getName());
        boolean isEnrolled = principal != null
                && enrollmentRepository.existsByStudentUsernameAndCourseId(principal.getName(), id);
        model.addAttribute("alreadyReviewed", alreadyReviewed);
        model.addAttribute("isEnrolled", isEnrolled);
        return "courses/detail";
    }

    @PostMapping("/courses/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String comment,
                               Principal principal) {
        try {
            reviewService.submit(principal.getName(), id, rating, comment);
        } catch (IllegalStateException | DuplicateEnrollmentException ex) {
            return "redirect:/courses/" + id + "?error=review";
        }
        return "redirect:/courses/" + id;
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
