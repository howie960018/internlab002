package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.CourseReview;
import com.ctbc.assignment2.exception.DuplicateEnrollmentException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.AppUserRepository;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.CourseReviewRepository;
import com.ctbc.assignment2.repository.EnrollmentRepository;
import com.ctbc.assignment2.service.CourseReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseReviewServiceImpl implements CourseReviewService {

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private CourseBeanRepository courseRepository;

    @Override
    public List<CourseReview> findByCourse(Long courseId) {
        return reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    @Override
    public CourseReview submit(String username, Long courseId, Integer rating, String comment) {
        if (!enrollmentRepository.existsByStudentUsernameAndCourseId(username, courseId)) {
            throw new IllegalStateException("Must be enrolled to review");
        }
        if (reviewRepository.existsByCourseIdAndReviewerUsername(courseId, username)) {
            throw new DuplicateEnrollmentException("Already reviewed this course");
        }
        AppUser reviewer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        CourseBean course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        CourseReview review = new CourseReview();
        review.setReviewer(reviewer);
        review.setCourse(course);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    @Override
    public void delete(Long reviewId) {
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        reviewRepository.delete(review);
    }

    @Override
    public Double getAvgRating(Long courseId) {
        Double avg = reviewRepository.findAvgRatingByCourseId(courseId);
        return avg != null ? avg : 0.0;
    }
}
