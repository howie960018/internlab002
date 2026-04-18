package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentUsernameAndCourseId(String username, Long courseId);
    List<Enrollment> findByStudentUsername(String username);
    long countByCourseId(Long courseId);
    Optional<Enrollment> findByStudentUsernameAndCourseId(String username, Long courseId);
}
