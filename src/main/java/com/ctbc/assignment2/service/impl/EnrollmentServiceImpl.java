package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.AppUser;
import com.ctbc.assignment2.bean.CourseBean;
import com.ctbc.assignment2.bean.Enrollment;
import com.ctbc.assignment2.exception.DuplicateEnrollmentException;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.AppUserRepository;
import com.ctbc.assignment2.repository.CourseBeanRepository;
import com.ctbc.assignment2.repository.EnrollmentRepository;
import com.ctbc.assignment2.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private CourseBeanRepository courseRepository;

    @Transactional
    @Override
    public Enrollment enroll(String username, Long courseId) {
        if (enrollmentRepository.existsByStudentUsernameAndCourseId(username, courseId)) {
            throw new DuplicateEnrollmentException("Already enrolled in course: " + courseId);
        }
        AppUser student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        CourseBean course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus("ACTIVE");
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public List<Enrollment> findByStudent(String username) {
        return enrollmentRepository.findByStudentUsername(username);
    }

    @Override
    public long countByCourse(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    @Transactional
    @Override
    public void cancel(String username, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentUsernameAndCourseId(username, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for course: " + courseId));
        enrollment.setStatus("CANCELLED");
        enrollmentRepository.save(enrollment);
    }
}
