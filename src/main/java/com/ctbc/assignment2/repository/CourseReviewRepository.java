package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    boolean existsByCourseIdAndReviewerUsername(Long courseId, String username);

    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.course.id = :courseId")
    Double findAvgRatingByCourseId(@Param("courseId") Long courseId);
}
