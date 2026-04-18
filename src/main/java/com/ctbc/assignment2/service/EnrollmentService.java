package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.Enrollment;
import java.util.List;

public interface EnrollmentService {
    Enrollment enroll(String username, Long courseId);
    List<Enrollment> findByStudent(String username);
    long countByCourse(Long courseId);
    void cancel(String username, Long courseId);
}
