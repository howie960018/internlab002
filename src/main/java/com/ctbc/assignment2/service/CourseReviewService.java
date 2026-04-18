package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.CourseReview;

import java.util.List;

public interface CourseReviewService {
    List<CourseReview> findByCourse(Long courseId);

    CourseReview submit(String username, Long courseId, Integer rating, String comment);

    void delete(Long reviewId);

    Double getAvgRating(Long courseId);
}
